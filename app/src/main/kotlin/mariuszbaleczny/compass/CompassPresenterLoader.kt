package mariuszbaleczny.compass

import android.content.Context
import mariuszbaleczny.compass.base.PresenterLoader
import mariuszbaleczny.compass.dagger.CompassComponent
import mariuszbaleczny.compass.dagger.DaggerCompassComponent
import mariuszbaleczny.compass.mvp.CompassMvp

/**
 * Created by mariusz on 25.11.16.
 */
class CompassPresenterLoader(context: Context?)
    : PresenterLoader<CompassMvp.Presenter>(context) {

    private var compassComponent: CompassComponent? = null

    override fun loadPresenter(): CompassMvp.Presenter? {
        if (compassComponent == null) {
            compassComponent = DaggerCompassComponent.builder().build()
        }
        return compassComponent?.getCompassPresenter()
    }

}