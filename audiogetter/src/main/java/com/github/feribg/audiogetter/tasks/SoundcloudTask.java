package com.github.feribg.audiogetter.tasks;


import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.models.Download;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class SoundcloudTask extends RawAudioTask {

    public SoundcloudTask(Integer id, Download download, int imageRes) {
        super(id, download, imageRes);
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        injector.injectMembersWithoutViews(this);

    }

}

