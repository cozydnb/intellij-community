// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.externalSystemIntegration.output.events;

import com.intellij.build.events.BuildEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.externalSystemIntegration.output.MavenOutputActionProcessor;

public interface MavenBuildEvent extends BuildEvent {

  void process(@NotNull MavenOutputActionProcessor processor);
}
