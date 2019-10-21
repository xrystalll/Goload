/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.api.transform.Transform;
import com.android.build.api.variant.VariantFilter;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.coverage.JacocoOptions;
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.dsl.AdbOptions;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.internal.dsl.TestOptions;
import com.android.build.gradle.internal.model.CoreExternalNativeBuild;
import com.android.builder.core.LibraryRequest;
import com.android.builder.model.DataBindingOptions;
import com.android.builder.model.SigningConfig;
import com.android.builder.testing.api.DeviceProvider;
import com.android.builder.testing.api.TestServer;
import com.android.repository.Revision;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.tasks.Internal;

/**
 * User configuration settings for all android plugins.
 */
public interface AndroidConfig {


    /**
     * Specifies the version of the <a
     * href="https://developer.android.com/studio/releases/build-tools.html">SDK Build Tools</a> to
     * use when building your project.
     *
     * <p>When using Android plugin 3.0.0 or later, configuring this property is optional. By
     * default, the plugin uses the minimum version of the build tools required by the <a
     * href="https://developer.android.com/studio/releases/gradle-plugin.html#revisions">version of
     * the plugin</a> you're using. To specify a different version of the build tools for the plugin
     * to use, specify the version as follows:
     *
     * <pre>
     * // Specifying this property is optional.
     * buildToolsVersion "26.0.0"
     * </pre>
     *
     * <p>For a list of build tools releases, read <a
     * href="https://developer.android.com/studio/releases/build-tools.html#notes">the release
     * notes</a>.
     *
     * <p>Note that the value assigned to this property is parsed and stored in a normalized form,
     * so reading it back may give a slightly different result.
     */
    String getBuildToolsVersion();

    /**
     * Specifies the API level to compile your project against. The Android plugin requires you to
     * configure this property.
     *
     * <p>This means your code can use only the Android APIs included in that API level and lower.
     * You can configure the compile sdk version by adding the following to the <code>android</code>
     * block: <code>compileSdkVersion 26</code>.
     *
     * <p>You should generally <a
     * href="https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels">use
     * the most up-to-date API level</a> available. If you are planning to also support older API
     * levels, it's good practice to <a
     * href="https://developer.android.com/studio/write/lint.html">use the Lint tool</a> to check if
     * you are using APIs that are not available in earlier API levels.
     *
     * <p>The value you assign to this property is parsed and stored in a normalized form, so
     * reading it back may return a slightly different value.
     */
    String getCompileSdkVersion();

    /**
     * This property is for internal use only.
     *
     * <p>To specify the version of the <a
     * href="https://developer.android.com/studio/releases/build-tools.html">SDK Build Tools</a>
     * that the Android plugin should use, use <a
     * href="com.android.build.gradle.BaseExtension.html#com.android.build.gradle.BaseExtension:buildToolsVersion">buildToolsVersion</a>
     * instead.
     */
    @Internal
    Revision getBuildToolsRevision();

    /**
     * Specifies the version of the module to publish externally. This property is generally useful
     * only to library modules that you intend to publish to a remote repository, such as Maven.
     *
     * <p>If you don't configure this property, the Android plugin publishes the release version of
     * the module by default. If the module