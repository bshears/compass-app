package mariuszbaleczny.compass.base

import android.content.Context
import android.support.v4.content.Loader
import mariuszbaleczny.compass.mvp.BasePresenter

/**
 * Created by mariusz on 25.11.16.
 */
abstract class PresenterLoader<P : BasePresenter>(context: Context?) : Loader<P>(context) {
    var presenter: P? = null

    override fun onStartLoading() {
        if (presenter != null) {
            deliverResult(presenter)
            return
        }
        onForceLoad()
    }

    protected abstract fun loadPresenter(): P?

    override fun onForceLoad() {
        presenter = loadPresenter()

        deliverResult(presenter)
    }

    override fun onReset() {
        if (presenter != null) {
            presenter = null
        }
    }

}