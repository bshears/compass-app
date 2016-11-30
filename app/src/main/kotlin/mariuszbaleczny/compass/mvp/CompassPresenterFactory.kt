package mariuszbaleczny.compass.mvp

import mariuszbaleczny.compass.base.PresenterFactory

/**
 * Created by mariusz on 25.11.16.
 */
class CompassPresenterFactory() : PresenterFactory<CompassPresenter> {

    override fun create(): CompassPresenter = CompassPresenter()

}