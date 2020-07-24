package pt.pak3nuh.hollywood.sandbox.clinic

enum class Exam(val cost: Int) {
    X_RAY(10),
    SOUND_SCAN(20),
    BLOOD_PRESSURE(5),
    ALLERGY_TEST(50),
    KIDNEY_EXAM(100)
}

sealed class ExamResult
object OkResult : ExamResult()
object NokResult : ExamResult()
