package com.raywenderlich.combineflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() {
    val flow1 = (1..10).asFlow().onEach { delay(1000L) }
    val flow2 = (10..20).asFlow().onEach { delay(30L) }

    //quando houver mudança no flow1 ou flow2 dispara
    runBlocking {
        flow1.combine(flow2) { n1, n2 ->
            println("$n1, $n2 ")
         }.launchIn(this)
    }
    //um espera o outro
    runBlocking {
        flow2.zip(flow1) { n1, n2 ->
            println("$n1, $n2 ")
        }.launchIn(this)
    }
//ninguem espera ninguem
    /* runBlocking {
         merge(flow1, flow2).onEach {
             println("$it \n")
         }.launchIn(this)
     }*/

}

class MainViewMOdel : ViewModel() {

    private val isAuthenticated = MutableStateFlow(true)
    private val user = MutableStateFlow<User?>(null)
    private val posts = MutableStateFlow(emptyList<Post>())

    private val _profileState = MutableStateFlow<ProfileState?>(null)
    val profileState = _profileState.asStateFlow()

    val flow1 = (1..10).asFlow().onEach { delay(1000L) }
    val flow2 = (10..20).asFlow().onEach { delay(300L) }


    init {
        //USE ASSIM
        user.combine(posts) { user, posts ->
            _profileState.value = profileState.value?.copy(
                profilePicUrl = user?.profilPicUrl,
                username = user?.username,
                description = user?.description,
                posts = posts
            )
        }.launchIn(viewModelScope)
        //OU ASSIM, é o mesmo que o acima
        /*viewModelScope.launch {
            user.combine(posts){user, post ->
                _profileState.value = profileState.value?.copy(
                    profilePicUrl =  user?.profilPicUrl,
                    username = user?.username,
                    description = user?.description,
                    posts = posts
                )
            }.collect()
        }*/

        //PARA USAR 3 FLOW
        isAuthenticated.combine(user) { isAuthenticated, user ->
            if (isAuthenticated) user else null
        }.combine(posts) { user, posts ->
            user?.let {
                _profileState.value = profileState.value?.copy(
                    profilePicUrl = user.profilPicUrl,
                    username = user.username,
                    description = user.description,
                    posts = posts
                )
            }


        }.launchIn(viewModelScope)

        //qndo flow1 o
        flow1.zip(flow2) { n1, n2 ->
            println("$n1, $n2 \n")
        }.launchIn(viewModelScope)

        //a cada emissoao ira cuspirr o flow
        merge(flow1, flow2).onEach {
            println("$it \n")
        }
    }
}