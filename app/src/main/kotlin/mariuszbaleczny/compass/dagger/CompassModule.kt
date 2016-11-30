package mariuszbaleczny.compass.dagger

import dagger.Module
import dagger.Provides
import mariuszbaleczny.compass.mvp.CompassMvp
import mariuszbaleczny.compass.mvp.CompassPresenter
import mariuszbaleczny.compass.ui.fragment.CompassFragment
import javax.inject.Singleton

/**
 * Created by mariusz on 24.11.16.
 */
@Module
class CompassModule {

    @Provides
    @Singleton
    fun provideCompassPresenter(): CompassMvp.Presenter {
        return CompassPresenter()
    }

    @Provides
    @Singleton
    fun provideCompassView(): CompassMvp.View {
        return CompassFragment()
    }

}