/*
 * Copyright 2017 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devsoap.plugin.actions

import com.devsoap.plugin.Util
import com.devsoap.plugin.tasks.CompileThemeTask
import com.devsoap.plugin.tasks.CompileWidgetsetTask
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.War

/**
 * Actions applied when the war plugin is added to the build
 *
 * @author John Ahlroos
 * @since 1.2
 */
class WarPluginAction extends PluginAction {

    @Override
    String getPluginId() {
        WarPlugin.WAR_TASK_NAME
    }

    @Override
    void apply(Project project) {
        super.apply(project)
        if (!springBootPresent) {
            // Apply the WAR plugin if spring boot is not present
            project.plugins.apply(WarPlugin)
        }
    }

    @Override
    protected void execute(Project project) {
        super.execute(project)
        War war = (War) project.tasks.getByName(WarPlugin.WAR_TASK_NAME)
        war.dependsOn(CompileWidgetsetTask.NAME)
        war.dependsOn(CompileThemeTask.NAME)
    }

    @Override
    protected void beforeTaskExecuted(Task task) {
        super.beforeTaskExecuted(task)
        switch (task.name) {
            case 'war':
                configureWAR(task)
                break
        }
    }

    private static configureWAR(Task task) {
        assert task in War
        War war = (War) task
        war.exclude('VAADIN/gwt-unitCache/**')
        if ( task.project.vaadin.manageDependencies ) {
            war.classpath = Util.getWarClasspath(task.project).files
        }
    }

    private boolean isSpringBootPresent() {
        try {
            getClass().classLoader.loadClass('org.springframework.boot.gradle.plugin.SpringBootPlugin')
            return true
        } catch (ClassNotFoundException e) {
            return false
        }
    }
}