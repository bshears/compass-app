package mariuszbaleczny.compass.base

import mariuszbaleczny.compass.mvp.BasePresenter

/**
 * Created by mariusz on 25.11.16.
 */
interface PresenterFactory<out P : BasePresenter> {

    fun create(): P

}