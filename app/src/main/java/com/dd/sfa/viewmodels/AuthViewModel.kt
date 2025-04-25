package com.dd.sfa.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dd.sfa.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// ViewModel that handles authentication and user data via Firebase.
class AuthViewModel : ViewModel() {

    // FirebaseAuth and Firestore instance to handle authentication and database.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData that holds the current user object (or null if not signed in).
    val currentUser = MutableLiveData<User?>()

    // Private mutable LiveData to track the current authentication state.
    private val _authState = MutableLiveData<AuthState>()
    // Public immutable LiveData for observers to watch the authentication state.
    val authState: LiveData<AuthState> = _authState

    // When the ViewModel is created, check the current authentication status.
    init {
        checkAuthStatus()
    }

    /**
     * Checks if a user is already authenticated.
     * - If no user is signed in, sets the state to Unauthenticated.
     * - If a user exists, sets the state to Authenticated and loads the user data.
     */
    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            val userId = auth.currentUser?.uid ?: return
            loadUserData(userId)
        }
    }

    /**
     * Logs in a user using the provided email and password.
     * If either field is empty, an error state is set.
     * Otherwise, it attempts to sign in using FirebaseAuth.
     */
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            // Error: Email and password must not be empty.
            _authState.value = AuthState.Error("Email and password must not be empty.")
            return
        }

        // Set the state to Loading while the login request is in progress.
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // On success, retrieve the user ID and load user data.
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    loadUserData(userId)
                } else {
                    // On failure, update the state with an error message.
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    /**
     * Registers a new user with the provided email and password.
     * If any required field is empty, sets an error state.
     * On successful registration, it saves the user data to Firestore.
     */
    fun register(
        email: String,
        password: String,
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            // Error: Email and password must not be empty.
            _authState.value = AuthState.Error("Email and password must not be empty.")
            return
        }

        // Set the state to Loading while the registration process is in progress.
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // On success, get the user ID.
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Build the data to store (with server timestamp).
                    val userData = hashMapOf(
                        "id" to userId,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    // Save the new user data to Firestore.
                    firebase.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            // If saving is successful, load the user data.
                            loadUserData(userId)
                        }
                        .addOnFailureListener { e ->
                            // If there's an error saving data, update the authentication state with the error.
                            _authState.value = AuthState.Error(e.message ?: "Error when saving the user data")
                        }
                } else {
                    // On registration failure, update the state with an error message.
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error during registration")
                }
            }
    }

    /**
     * Signs out the current user.
     * Sets the authentication state to Unauthenticated and clears currentUser.
     */
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        currentUser.value = null
    }

    /**
     * Loads the user data from Firestore based on the provided user ID.
     *
     * @param userId The ID of the user whose data should be loaded.
     * If the user data exists, updates the currentUser LiveData and sets the state to Authenticated.
     * Otherwise, sets an error state.
     */
    private fun loadUserData(userId: String) {
        if (userId.isEmpty()) {
            // Log an error if the user ID is null or empty.
            println("Error: User ID is null or empty")
            return
        }
        firebase.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Convert the document to a User object.
                    val user = document.toObject(User::class.java)
                    // Update the LiveData holding the current user.
                    currentUser.value = user
                    // Set the authentication state to AuthState.Authenticated.
                    _authState.value = AuthState.Authenticated
                } else {
                    // If no user data is found, set an error state.
                    _authState.value = AuthState.Error("User data not found")
                }
            }
            .addOnFailureListener { e ->
                // On failure, update the state with an error message.
                _authState.value = AuthState.Error(e.message ?: "Error loading user data")
            }
    }
}

// A sealed class to represent the various states of authentication.
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
