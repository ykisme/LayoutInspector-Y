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

import com.android.ddmlib.Client;
import com.android.layoutinspector.model.ClientWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBRadioButton;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yk.plugin.layoutinspector.config.Constant;
import yk.plugin.layoutinspector.config.MyPluginSettings;
import yk.plugin.layoutinspector.config.PrefVersion;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WindowPickerDialog extends DialogWrapper {
    @NonNls
    private static final String WINDOW_PICKER_DIMENSIONS_KEY = "LayoutInspector.WindowPicker.Options.Dimensions";

    private final JPanel myPanel;
    private final JComboBox myWindowsCombo;

    @Nullable ClientWindow mySelectedWindow;
    @Nullable PrefVersion mPrefVersion;

    public WindowPickerDialog(@NotNull Project project, @NotNull final Client client, @NotNull List<ClientWindow> windows) {
        super(project, true);
        setTitle(AndroidBundle.message("android.ddms.actions.layoutinspector.windowpicker"));

        myPanel = new JPanel(new BorderLayout());

        myWindowsCombo = new ComboBox(new CollectionComboBoxModel<ClientWindow>(windows));
        myWindowsCombo.setRenderer(SimpleListCellRenderer.create("", ClientWindow::getDisplayName));
        myWindowsCombo.setSelectedIndex(0);
        myPanel.add(myWindowsCombo, BorderLayout.CENTER);
        // add perfversion settings
        if (MyPluginSettings.getInstance().getState().getPrefVersion() == PrefVersion.ASK) {
            initPrefVersionButtons(myPanel);
        }
        init();
    }

    private void initPrefVersionButtons(JPanel panel) {
        JPanel btnsContainer = new JPanel();
        btnsContainer.setLayout(new BoxLayout(btnsContainer, BoxLayout.Y_AXIS)); // 垂直排列
        JBRadioButton v1Button = new JBRadioButton(Constant.V1_BTN_DES);
        v1Button.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnsContainer.add(v1Button);
        btnsContainer.add(Box.createRigidArea(new Dimension(0, 10))); // 添加间距
        v1Button.addActionListener(e -> {
            if (v1Button.isSelected()) {
                mPrefVersion = PrefVersion.V1;
            }
        });
        JBRadioButton v2Button = new JBRadioButton(Constant.V2_BTN_DES);
        v2Button.setAlignmentX(Component.LEFT_ALIGNMENT);
        v2Button.addChangeListener(e -> {
            if (v2Button.isSelected()) {
                mPrefVersion = PrefVersion.V2;
            }
        });
        btnsContainer.add(v2Button);
        btnsContainer.add(Box.createRigidArea(new Dimension(0, 10))); // 添加间距
        ButtonGroup group = new ButtonGroup();
        group.add(v1Button);
        group.add(v2Button);
        panel.add(btnsContainer, BorderLayout.NORTH);
        v1Button.setSelected(true);
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
        Object selection = myWindowsCombo.getSelectedItem();
        if (selection instanceof ClientWindow) {
            mySelectedWindow = (ClientWindow) selection;
        }
        super.doOKAction();
    }

    @Nullable
    public ClientWindow getSelectedWindow() {
        return mySelectedWindow;
    }

    @Nullable
    public PrefVersion getSelectedPrefVersion() {
        return mPrefVersion;
    }
}
