package com.android.toolsy.idea.editors.layoutInspector.actions;

import com.android.ddmlib.Client;
import com.android.layoutinspectory.model.ClientWindow;
import com.android.toolsy.idea.editors.layoutInspector.LayoutInspectorCaptureTask;
import com.android.toolsy.idea.editors.layoutInspector.PrefVersionPickerDialog;
import com.android.toolsy.idea.editors.layoutInspector.WindowPickerDialog;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yk.plugin.layoutinspector.config.MyPluginSettings;
import yk.plugin.layoutinspector.config.PrefVersion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@VisibleForTesting
interface ClientWindowRetriever {
    default List<ClientWindow> getAllWindows(@NotNull Client client, long timeout, @NotNull TimeUnit timeoutUnits) throws IOException {
        return ClientWindow.getAll(client, timeout, timeoutUnits);
    }
}

public class GetClientWindowsTask extends Task.Backgroundable {
    private final Client myClient;
    private List<ClientWindow> myWindows;
    private String myError;

    @NotNull
    private final ClientWindowRetriever myClientWindowRetriever;

    @VisibleForTesting
    GetClientWindowsTask(@Nullable Project project, @NotNull Client client, @NotNull ClientWindowRetriever windowRetriever) {
        super(project, "Obtaining Windows");
        myClient = client;
        myError = null;

        myClientWindowRetriever = windowRetriever;
    }

    public GetClientWindowsTask(@Nullable Project project, @NotNull Client client) {
        this(project, client, new ClientWindowRetriever() {
        });
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);

        try {
            myWindows = myClientWindowRetriever.getAllWindows(myClient, 5, TimeUnit.SECONDS);
            myWindows = sortWindow(myWindows);
            if (myWindows == null) {
                myError = "Unable to obtain list of windows used by " +
                        myClient.getClientData().getPackageName() +
                        "\nLayout Inspector requires device API version to be 18 or greater.";
            } else if (myWindows.isEmpty()) {
                myError = "No active windows displayed by " + myClient.getClientData().getPackageName();
            }
        } catch (IOException e) {
            myError = "Unable to obtain list of windows used by " + myClient.getClientData().getPackageName() + "\nError: " + e.getMessage();
        }
    }

    @Override
    public void onSuccess() {
        if (myError != null) {
            Messages.showErrorDialog(myError, "Capture View Hierarchy");
            return;
        }

        ClientWindow window;
        PrefVersion prefVersion = null;
        if (myWindows.size() == 1) {
            window = myWindows.get(0);
            if (MyPluginSettings.getInstance().getState().getPrefVersion() == PrefVersion.ASK) {
                PrefVersionPickerDialog pickerDialog = new PrefVersionPickerDialog(myProject);
                if (!pickerDialog.showAndGet()) {
                    return;
                }
                prefVersion = pickerDialog.getSelectedPrefVersion();
            }
        } else { // prompt user if there are more than 1 windows displayed by this application
            WindowPickerDialog pickerDialog = new WindowPickerDialog(myProject, myClient, myWindows);
            if (!pickerDialog.showAndGet()) {
                return;
            }

            window = pickerDialog.getSelectedWindow();
            if (window == null) {
                return;
            }
            prefVersion = pickerDialog.getSelectedPrefVersion();
        }

        LayoutInspectorCaptureTask captureTask = new LayoutInspectorCaptureTask(myProject, myClient, window, prefVersion);
        captureTask.queue();
    }

    boolean matchPref(String disName, List<String> pref) {
        for (String str : pref) {
            if (disName.startsWith(str)) return true;
        }
        return false;
    }

    public List<ClientWindow> sortWindow(List<ClientWindow> windows) {
        Map<String, List<String>> prefWindows = MyPluginSettings.getInstance().getState().getPrefWindowMap();
        String proc = myClient.getClientData().getClientDescription();
        List<String> windowNames = prefWindows.get(proc);
        if (windowNames == null || windowNames.isEmpty()) return windows;
        ArrayList<ClientWindow> res = new ArrayList<>(windows.size());
        for (ClientWindow cw : windows) {
            if (cw.getDisplayName() != null && matchPref(cw.getDisplayName(), windowNames)) {
                res.add(0, cw);
            } else {
                res.add(cw);
            }
        }
        return res;
    }
}

