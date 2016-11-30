package mariuszbaleczny.compass.loader

import android.content.Context
import mariuszbaleczny.compass.dagger.CompassComponent
import mariuszbaleczny.compass.dagger.DaggerCompassComponent
import mariuszbaleczny.compass.mvp.CompassMvp.Presenter

/**
 * Created by mariusz on 25.11.16.
 */
class CompassPresenterLoader(context: Context?)
    : PresenterLoader<Presenter>(context) {

    private var compassComponent: CompassComponent? = null

    override fun loadPresenter(): Presenter? {
        if (compassComponent == null) {
            compassComponent = DaggerCompassComponent.builder().build()
        }
        return compassComponent?.getCompassPresenter()
    }

}