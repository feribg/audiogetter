package com.github.feribg.audiogetter.tasks.download;


import com.github.feribg.audiogetter.config.App;
import com.github.feribg.audiogetter.models.Download;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class YoutubeTask extends VideoTask {

    public YoutubeTask(Integer id, Download download, int iconRes) {
        super(id, download, iconRes);
        final RoboInjector injector = RoboGuice.getInjector(App.ctx);
        injector.injectMembersWithoutViews(this);

    }

}

