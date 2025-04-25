package com.dd.sfa.data

import com.google.firebase.Timestamp

data class User(
    var id: String = "",
    var email: String = "",
    var createdAt: Timestamp? = null
)

data class CustomExercise(
    var id: String = "",
    var exerciseName: String = "",
    var muscleGroup: String = "",
    var description: String = ""
)

data class TemplateExercise(
    var id: String = "",
    var exerciseName: String = "",
    var muscleGroup: String = "",
    var description: String = ""
)

data class TrainingPlan(
    var id: String = "",
    var planName: String = "",
    var muscleGroups: String = "",
    var description: String = "",
    var createdAt: Timestamp? = null
)

data class Exercise(
    var id: String = "",
    var exerciseName: String = "",
    var muscleGroup: String = "",
    var description: String = "",
    var order: Int? = null
)

data class WorkoutSet(
    var id: String = "",
    var setNumber: Int? = null,
    var reps: Int? = null,
    var weight: Double? = null,
    var performedAt: Timestamp? = null
)
