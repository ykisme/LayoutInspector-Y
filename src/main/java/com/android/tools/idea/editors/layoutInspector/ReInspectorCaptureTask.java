package com.android.tools.idea.editors.layoutInspector;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.layoutinspector.LayoutInspectorResult;
import com.android.layoutinspector.model.ClientWindow;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yk.plugin.layoutinspector.config.PrefVersion;
import yk.plugin.layoutinspector.utils.ClientUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ReInspectorCaptureTask extends Task.Backgroundable {
    private static final String TITLE = "Capture View Hierarchy";

    @NotNull
    private final String myWindow;
    @Nullable
    private final String myDeviceSeri;
    private String myError;
    @Nullable
    private PrefVersion mPrefVersion;
    @Nullable
    private final String myDeviceName;
    @NotNull
    private final String myClientName;
    @Nullable
    private ClientWindow myClientWindow;
    @Nullable
    private Client myClient;
    private AndroidDebugBridge myAdb;

    public ReInspectorCaptureTask(@NotNull Project project, @Nullable String deviceName, @Nullable String deviceSeri,
                                  @NotNull String clientName,
                                  @Nullable PrefVersion prefVersion,
                                  @NotNull String windowName) {
        super(project, "Capturing View Hierarchy");
        myDeviceName = deviceName;
        myDeviceSeri = deviceSeri;
        myWindow = windowName;
        mPrefVersion = prefVersion;
        myClientName = clientName;
        myAdb = AndroidSdkUtils.getDebugBridge(project);
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {

        Client client = ClientUtils.selectClient(myAdb, myDeviceName,
                myDeviceSeri, myClientName);
        myClient = client;
        if (client != null) {
            try {
                List<@NotNull ClientWindow> all = ClientWindow.getAll(client, 5, TimeUnit.SECONDS);
                if (all == null || all.isEmpty()) {
                    myError = "not find window";
                    return;
                }
                String prefix = myWindow;
                var res = all.stream().filter((i) -> Objects.equals(i.getDisplayName(), prefix)).findFirst();
                res.ifPresentOrElse((it) -> myClientWindow = it, () -> myError = "not find window");
            } catch (IOException ignore) {
            }
        } else {
            myError = "not find device";
        }
    }

    @Override
    public void onSuccess() {
        if (myError != null) {
            Messages.showErrorDialog("Error obtaining view hierarchy: " + StringUtil.notNullize(myError), TITLE);
            return;
        }
        LayoutInspectorCaptureTask captureTask = new LayoutInspectorCaptureTask(myProject,
                myClient, myClientWindow, mPrefVersion);
        captureTask.queue();
    }
}
