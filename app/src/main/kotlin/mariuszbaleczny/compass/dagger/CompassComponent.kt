package mariuszbaleczny.compass.dagger

import dagger.Component
import mariuszbaleczny.compass.mvp.CompassMvp
import mariuszbaleczny.compass.ui.activity.CompassActivity
import javax.inject.Singleton

/**
 * Created by mariusz on 24.11.16.
 */
@Singleton
@Component(modules = arrayOf(CompassModule::class))
interface CompassComponent {

    fun inject(compassActivity: CompassActivity)

    fun getCompassPresenter(): CompassMvp.Presenter

}