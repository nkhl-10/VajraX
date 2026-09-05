package com.vajrax.platform

/**
 * Phase 5: Passive Intelligence OS Wrapper.
 * Allows VAJRAX to track what the phone already knows (Zero Friction rule).
 * Implemented natively via Android Health Connect & iOS HealthKit.
 */
interface PassiveIntelligenceController {
    
    /** Returns total steps taken today */
    suspend fun getStepsForToday(): Int
    
    /** Returns sleep duration from the previous night in minutes */
    suspend fun getSleepDurationMinutes(): Int
    
    /** Returns time spent on distraction apps today (for Daṇḍa / Reality checks) */
    suspend fun getDistractionMinutesForToday(): Int
}
