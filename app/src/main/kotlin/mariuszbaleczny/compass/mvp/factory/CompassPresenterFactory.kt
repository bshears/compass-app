package mariuszbaleczny.compass.mvp.factory

import mariuszbaleczny.compass.mvp.CompassPresenter

/**
 * Created by mariusz on 25.11.16.
 */
class CompassPresenterFactory() : PresenterFactory<CompassPresenter> {

    override fun create(): CompassPresenter = CompassPresenter()

}