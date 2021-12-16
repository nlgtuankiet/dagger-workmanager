package com.sample.daggerworkmanagersample

import dagger.Component

@Component
interface SampleComponent {
    fun injectTo(application: SampleApplication)
}


