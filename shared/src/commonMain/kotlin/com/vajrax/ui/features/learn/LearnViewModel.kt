package com.vajrax.ui.features.learn

import com.vajrax.domain.repository.LearnRepository
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class LearnViewModel(
    private val learnRepository: LearnRepository
) : MviViewModel<LearnUiState, LearnIntent, LearnEffect>(LearnUiState()) {

    init {
        sendIntent(LearnIntent.LoadIdeas)
    }

    override fun sendIntent(intent: LearnIntent) {
        when (intent) {
            is LearnIntent.LoadIdeas -> loadIdeas()
            is LearnIntent.StartApplyFlow -> updateState { copy(isApplyingIdeaId = intent.ideaId, userCustomPracticeText = "") }
            is LearnIntent.UpdatePracticeText -> updateState { copy(userCustomPracticeText = intent.text) }
            is LearnIntent.CancelApplyFlow -> updateState { copy(isApplyingIdeaId = null, userCustomPracticeText = "") }
            is LearnIntent.Launch7DayExperiment -> launchExperiment(intent.ideaId, intent.practiceText)
        }
    }

    private fun loadIdeas() {
        viewModelScope.launch(Dispatchers.IO) {
            val ideas = learnRepository.getAllBookIdeas()
            updateState { copy(ideas = ideas) }
        }
    }

    private fun launchExperiment(ideaId: String, customPractice: String) {
        viewModelScope.launch(Dispatchers.IO) {
            learnRepository.launchExperiment(ideaId, customPractice, 7)
            val updated = learnRepository.getAllBookIdeas()
            updateState {
                copy(
                    ideas = updated,
                    isApplyingIdeaId = null,
                    userCustomPracticeText = ""
                )
            }
            sendEffect(LearnEffect.ShowToast("7-Day Experiment Active: Theory converted to Practice."))
        }
    }
}
