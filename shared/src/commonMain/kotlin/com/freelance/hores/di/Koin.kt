package com.freelance.hores.di

import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.ui.screen.calendari.CalendariViewModel
import com.freelance.hores.ui.screen.clients.ClientsViewModel
import com.freelance.hores.ui.screen.dia.DiaDetallViewModel
import com.freelance.hores.ui.screen.fitxar.FitxarViewModel
import com.freelance.hores.ui.screen.registre.RegistreViewModel
import com.freelance.hores.ui.screen.resum.ResumViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    single { RegistreRepository(get(), get(), get(), get(), get()) }

    // A Koin 3.5.x, viewModel{} del DSL no existeix al commonMain KMP.
    // S'usa factory{} aquí. koin-compose:1.1.2 gestiona el cicle de vida
    // correctament via koinViewModel() gràcies a lifecycle-viewmodel de AndroidX.
    factory { CalendariViewModel(get()) }
    factory { ClientsViewModel(get()) }
    factory { DiaDetallViewModel(get()) }
    factory { FitxarViewModel(get(), get()) }
    factory { RegistreViewModel(get()) }
    factory { ResumViewModel(get(), get()) }
}

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(commonModule(), platformModule())
    }
}
