package com.dd.sfa

import com.dd.sfa.data.*
import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class DataModelUnitTest {

    @Test
    fun customExercise_equalityAndCopy() {
        val original = CustomExercise(
            id = "ex1",
            exerciseName = "Push-Up",
            muscleGroup = "Chest",
            description = "Bodyweight press"
        )

        // equality by value
        val same = original.copy()
        assertEquals(original, same)
        assertEquals(original.hashCode(), same.hashCode())

        // changing one property breaks equality
        val changed = original.copy(exerciseName = "Pull-Up")
        assertNotEquals(original, changed)
        assertEquals("Pull-Up", changed.exerciseName)
    }

    @Test
    fun templateExercise_equalityAndHashCode() {
        val t1 = TemplateExercise("t1", "Squat", "Legs", "")
        val t2 = TemplateExercise("t1", "Squat", "Legs", "")
        assertTrue(t1 == t2)
        assertEquals(t1.hashCode(), t2.hashCode())

        val t3 = t2.copy(muscleGroup = "Full Body")
        assertFalse(t1 == t3)
    }

    @Test
    fun trainingPlan_copyPreservesAndModifies() {
        val now = Timestamp.now()
        val plan = TrainingPlan(
            id = "p1",
            planName = "Strength",
            muscleGroups = "Full Body",
            description = "3-day split",
            createdAt = now
        )

        // copy with no args yields equal instance
        val clone = plan.copy()
        assertEquals(plan, clone)

        // copy with changed fields
        val updated = plan.copy(planName = "Hypertrophy")
        assertNotEquals(plan, updated)
        assertEquals("Hypertrophy", updated.planName)
        assertEquals(now, updated.createdAt)
    }

    @Test
    fun exercise_defaultsAndCopy() {
        val e = Exercise(
            id = "e1",
            exerciseName = "Bench Press",
            muscleGroup = "Chest",
            description = "Barbell press",
            order = null
        )
        // default order should be null
        assertNull(e.order)

        // copy and assign order
        val withOrder = e.copy(order = 1)
        assertEquals(1, withOrder.order)
        assertNotEquals(e, withOrder)
    }

    @Test
    fun workoutSet_copyAndEqualityIgnoringTimestamp() {
        // create a timestamp for test
        val t1 = Timestamp(TimeUnit.SECONDS.toMillis(1000), 0)
        val set1 = WorkoutSet(
            id = "s1",
            setNumber = 1,
            reps = 10,
            weight = 80.0,
            performedAt = t1
        )

        // full copy preserves equality
        val setClone = set1.copy()
        assertEquals(set1, setClone)

        // changing performedAt yields different instance
        val t2 = Timestamp(TimeUnit.SECONDS.toMillis(2000), 0)
        val differentTime = set1.copy(performedAt = t2)
        assertNotEquals(set1, differentTime)
        assertEquals(10, differentTime.reps)
    }
}
