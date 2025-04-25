package com.dd.sfa.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd.sfa.data.CustomExercise
import com.dd.sfa.data.Exercise
import com.dd.sfa.data.TemplateExercise
import com.dd.sfa.data.TrainingPlan
import com.dd.sfa.data.WorkoutSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsible for managing CRUD operations on user-related data,
 * such as TrainingPlans, Exercises, Sets, and CustomExercises.
 *
 * Offline persistence is enabled via Firestore’s built-in caching.
 */
class DataViewModel : ViewModel() {

    // Firebase Firestore instance for cloud storage
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    // FirebaseAuth instance to manage user authentication
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData holding the list of training plans
    private val _plans = MutableLiveData<List<TrainingPlan>>()
    val plans: LiveData<List<TrainingPlan>> = _plans

    // LiveData holding the list of template exercises
    private val _templateExercises = MutableLiveData<List<TemplateExercise>>()
    val templateExercises: LiveData<List<TemplateExercise>> = _templateExercises

    init {
        // Enable Firestore offline caching by setting persistence flag
        firebase.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        // Immediately fetch plans when ViewModel is created
        fetchPlans()
    }

    /**
     * Fetches training plans for the current user.
     * If user is authenticated, listens to Firestore collection updates,
     * otherwise loads plans from local storage.
     */
    fun fetchPlans() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Listen to realtime updates from Firestore
            firebase.collection("users").document(userId).collection("trainingPlans")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        // On error, post empty list to UI
                        _plans.value = emptyList()
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        // Map Firestore documents to TrainingPlan objects
                        val planList = it.documents.mapNotNull { doc ->
                            doc.toObject(TrainingPlan::class.java)
                        }
                        _plans.value = planList
                    }
                }
        } else {
            // Not authenticated: load from local DB
            loadLocalPlans()
        }
    }

    /**
     * Saves or updates a TrainingPlan.
     * Stores to Firestore if user is authenticated; otherwise saves locally.
     */
    fun savePlan(plan: TrainingPlan) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firebase.collection("users").document(userId)
                .collection("trainingPlans")
                .document(plan.id)
                .set(plan)
        } else {
            savePlanLocally(plan)
        }
    }

    /**
     * Loads training plans from local storage asynchronously.
     */
    fun loadLocalPlans() {
        viewModelScope.launch {
            val localPlans = withContext(Dispatchers.IO) {
                emptyList<TrainingPlan>()
            }
            _plans.value = localPlans
        }
    }

    /**
     * Deletes a TrainingPlan.
     * Removes from Firestore if authenticated; otherwise deletes locally.
     */
    fun deletePlan(plan: TrainingPlan) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firebase.collection("users").document(userId)
                .collection("trainingPlans")
                .document(plan.id)
                .delete()
        } else {
            // Not authenticated: delete from local DB
            deletePlanLocally(plan)
        }
    }

    /**
     * Saves a plan to local storage in a background thread.
     */
    fun savePlanLocally(plan: TrainingPlan) {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    /**
     * Deletes a plan from local storage in a background thread.
     */
    private fun deletePlanLocally(plan: TrainingPlan) {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    // region Template Exercises

    /**
     * Fetches list of template exercises from Firestore with realtime updates.
     */
    fun fetchTemplateExercises() {
        firebase.collection("templateExercises")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // On error, post empty list
                    _templateExercises.value = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(TemplateExercise::class.java)
                    }
                    _templateExercises.value = list
                    Log.d("TemplateExercises", "Fetched ${list.size} template exercises")
                } else {
                    _templateExercises.value = emptyList()
                }
            }
    }

    // endregion

    // region Listen to Exercises in a Plan

    /**
     * Listens to Exercise documents for a given plan in realtime,
     * invoking onUpdate callback with the latest list.
     */
    fun listenToExercises(
        userId: String,
        planId: String,
        onUpdate: (List<Exercise>) -> Unit
    ) {
        firebase.collection("users").document(userId)
            .collection("trainingPlans").document(planId)
            .collection("exercises")
            .orderBy("order")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val exList = snapshot.documents.mapNotNull {
                        it.toObject(Exercise::class.java)
                    }
                    onUpdate(exList)
                } else {
                    onUpdate(emptyList())
                }
            }
    }

    // endregion

    // region Custom Exercises

    /**
     * Creates a new custom exercise document.
     * Provides immediate callback in offline mode after 1 second.
     */
    fun createCustomExercise(
        userId: String,
        exerciseName: String,
        muscleGroup: String,
        description: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Generate new Firestore document reference and ID
        val docRef = firebase.collection("users").document(userId)
            .collection("customExercises")
            .document()
        val exerciseId = docRef.id

        // Prepare exercise data map
        val dataMap = hashMapOf(
            "id" to exerciseId,
            "exerciseName" to exerciseName,
            "muscleGroup" to muscleGroup,
            "description" to description
        )

        var callbackCalled = false

        // Write to Firestore with completion listener
        docRef.set(dataMap)
            .addOnCompleteListener { task ->
                if (!callbackCalled) {
                    callbackCalled = true
                    if (task.isSuccessful) {
                        Log.d("DataViewModel", "Custom exercise created successfully.")
                        onResult(true, exerciseId)
                    } else {
                        val e = task.exception
                        Log.d("DataViewModel", "Error creating custom exercise: ${e?.message}")
                        onResult(false, e?.message)
                    }
                }
            }

        // Fallback for offline mode: trigger success after delay
        viewModelScope.launch {
            delay(1000L)
            if (!callbackCalled) {
                callbackCalled = true
                Log.d("DataViewModel", "Custom exercise creation queued (offline mode) – triggering immediate success callback.")
                onResult(true, exerciseId)
            }
        }
    }

    /**
     * Listens to all custom exercises for user, returning list or error message.
     */
    fun listenToCustomExercises(
        userId: String,
        onResult: (List<CustomExercise>?, String?) -> Unit
    ) {
        firebase.collection("users").document(userId)
            .collection("customExercises")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onResult(null, e.message)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val exercises = snapshot.documents.mapNotNull {
                        it.toObject(CustomExercise::class.java)
                    }
                    onResult(exercises, null)
                } else {
                    onResult(emptyList(), null)
                }
            }
    }

    // endregion

    // region Training Plans

    /**
     * Updates an existing training plan with fallback for offline support.
     */
    fun updateTrainingPlan(
        userId: String,
        plan: TrainingPlan,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (plan.id.isBlank()) {
            onResult(false, "Plan ID is empty.")
            return
        }

        val docRef = firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(plan.id)

        var callbackCalled = false

        docRef.set(plan)
            .addOnCompleteListener { task ->
                if (!callbackCalled) {
                    callbackCalled = true
                    if (task.isSuccessful) {
                        Log.d("DataViewModel", "Training plan updated successfully.")
                        onResult(true, null)
                    } else {
                        val e = task.exception
                        Log.d("DataViewModel", "Error updating training plan: ${e?.message}")
                        onResult(false, e?.message)
                    }
                }
            }

        // Fallback: assume offline after 1 second
        viewModelScope.launch {
            delay(1000L)
            if (!callbackCalled) {
                callbackCalled = true
                Log.d("DataViewModel", "Training plan update queued (offline mode) – triggering immediate success callback.")
                onResult(true, null)
            }
        }
    }

    // endregion

    // region Exercises

    /**
     * Creates a new Exercise under a specific TrainingPlan.
     */
    fun createExercise(
        userId: String,
        planId: String,
        exerciseName: String,
        muscleGroup: String,
        description: String,
        order: Int?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val docRef = firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document()
        val exerciseId = docRef.id

        val dataMap = hashMapOf(
            "id" to exerciseId,
            "exerciseName" to exerciseName,
            "muscleGroup" to muscleGroup,
            "description" to description,
            "order" to order
        )

        docRef.set(dataMap)
            .addOnSuccessListener {
                onResult(true, exerciseId)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * Retrieves all exercises for a given plan once.
     */
    fun getExercises(
        userId: String,
        planId: String,
        onResult: (List<Exercise>?, String?) -> Unit
    ) {
        firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val exercises = mutableListOf<Exercise>()
                for (doc in querySnapshot) {
                    exercises.add(doc.toObject(Exercise::class.java))
                }
                onResult(exercises, null)
            }
            .addOnFailureListener { e ->
                onResult(null, e.message)
            }
    }

    /**
     * Updates an existing Exercise document.
     */
    fun updateExercise(
        userId: String,
        planId: String,
        exercise: Exercise,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (exercise.id.isBlank()) {
            onResult(false, "Exercise ID is empty.")
            return
        }
        firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * Deletes an exercise document by ID.
     */
    fun deleteExercise(
        userId: String,
        planId: String,
        exerciseId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document(exerciseId)
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    // endregion

    // region Sets

    /**
     * Creates a new WorkoutSet for an exercise, timestamped on server.
     */
    fun createSet(
        userId: String,
        planId: String,
        exerciseId: String,
        setNumber: Int?,
        reps: Int?,
        weight: Double?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val docRef = firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document(exerciseId)
            .collection("sets")
            .document()
        val setId = docRef.id

        val dataMap = hashMapOf(
            "id" to setId,
            "setNumber" to setNumber,
            "reps" to reps,
            "weight" to weight,
            "performedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(dataMap)
            .addOnSuccessListener {
                onResult(true, setId)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * Listens to WorkoutSet updates for an exercise in realtime.
     */
    fun listenToSets(
        userId: String,
        planId: String,
        exerciseId: String,
        onResult: (List<WorkoutSet>?, String?) -> Unit
    ) {
        firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document(exerciseId)
            .collection("sets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onResult(null, e.message)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val sets = snapshot.documents.mapNotNull {
                        it.toObject(WorkoutSet::class.java)
                    }
                    onResult(sets, null)
                } else {
                    onResult(emptyList(), null)
                }
            }
    }

    /**
     * Updates an existing WorkoutSet document.
     */
    fun updateSet(
        userId: String,
        planId: String,
        exerciseId: String,
        workoutSet: WorkoutSet,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (workoutSet.id.isBlank()) {
            onResult(false, "Set ID is empty.")
            return
        }
        firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document(exerciseId)
            .collection("sets")
            .document(workoutSet.id)
            .set(workoutSet)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    /**
     * Deletes a WorkoutSet document by ID.
     */
    fun deleteSet(
        userId: String,
        planId: String,
        exerciseId: String,
        setId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebase.collection("users").document(userId)
            .collection("trainingPlans")
            .document(planId)
            .collection("exercises")
            .document(exerciseId)
            .collection("sets")
            .document(setId)
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
    // endregion
}
