package pt.pak3nuh.hollywood.sandbox.actor

enum class Treatment(val cost: Int, val timeInSec: Int) {
    APPLY_CAST(50, 2),
    HEART_CIRGURY(1000, 5),
    BLOOD_THINNER(40, 2),
    CORTIZONE(60, 1),
    KIDNEY_TRANSPLANT(5000, 6)
}
