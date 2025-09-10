/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.editors.layoutInspector;

import com.android.tools.r8.graph.V2;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBRadioButton;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yk.plugin.layoutinspector.config.Constant;
import yk.plugin.layoutinspector.config.PrefVersion;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PrefVersionPickerDialog extends DialogWrapper {


    @NonNls
    private static final String WINDOW_PICKER_DIMENSIONS_KEY = "LayoutInspector.WindowPicker.Options.Dimensions";

    private final JPanel myPanel;
    @Nullable PrefVersion mPrefVersion;

    public PrefVersionPickerDialog(@NotNull Project project) {
        super(project, true);
        setTitle(Constant.TITLE_SELECT_VERSION);

        myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS)); // 垂直排列
        JBRadioButton v1Button = new JBRadioButton(Constant.V1_BTN_DES);
        v1Button.setAlignmentX(Component.LEFT_ALIGNMENT);
        myPanel.add(v1Button);
        myPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 添加间距
        v1Button.addActionListener(e -> {
            if (v1Button.isSelected()) {
                mPrefVersion = PrefVersion.V1;
                performOKAction();
            }
        });
        JBRadioButton v2Button = new JBRadioButton(Constant.V2_BTN_DES);
        v2Button.setAlignmentX(Component.LEFT_ALIGNMENT);
        v2Button.addChangeListener(e -> {
            if (v2Button.isSelected()) {
                mPrefVersion = PrefVersion.V2;
                performOKAction();
            }
        });
        myPanel.add(v2Button);
        myPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 添加间距
        JLabel textField = new JLabel(Constant.PREF_SETTING_INFO);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        myPanel.add(textField);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return WINDOW_PICKER_DIMENSIONS_KEY;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Nullable
    public PrefVersion getSelectedPrefVersion() {
        return mPrefVersion;
    }
}
