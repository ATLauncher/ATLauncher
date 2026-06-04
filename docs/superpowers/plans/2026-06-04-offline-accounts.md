# Offline Accounts (accountless offline mode) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let ATLauncher launch Minecraft in offline mode with no Microsoft account, via a self-contained offline-account subsystem (new `OfflineAccount` type, `OfflineAccountManager`, dedicated "Offline Accounts" tab), generalizing the launch pipeline from `MicrosoftAccount` to `AbstractAccount`.

**Architecture:** Offline accounts are a parallel subsystem to Microsoft accounts — their own data class, manager, storage file (`offlineaccounts.json`), and UI tab — so almost all new code lives in new files and edits to upstream files are small and localized (for merge-friendliness). The launch path accepts `AbstractAccount` and guards Microsoft-specific calls with `instanceof MicrosoftAccount`.

**Tech Stack:** Java 11, Gradle, JUnit 5 (Jupiter), Gson, RxJava 3 (`BehaviorSubject`), Swing.

**Reference spec:** `docs/superpowers/specs/2026-06-04-offline-accounts-design.md`

---

## File Structure

**New files:**
- `src/main/java/com/atlauncher/data/OfflineAccount.java` — synthetic account (token `"0"`, UUID from username).
- `src/main/java/com/atlauncher/managers/OfflineAccountManager.java` — list + selected offline account, persistence to `offlineaccounts.json`.
- `src/main/java/com/atlauncher/gui/tabs/offlineaccounts/OfflineAccountsTab.java` — minimal tab (add/delete/select), talks directly to `OfflineAccountManager`.
- `src/test/java/com/atlauncher/data/OfflineAccountTest.java` — unit tests for the account + serialization.

**Edited files (small/localized):**
- `src/main/java/com/atlauncher/FileSystem.java` — add `OFFLINE_ACCOUNTS` path.
- `src/main/java/com/atlauncher/data/Settings.java` — add `lastOfflineAccount` field.
- `src/main/java/com/atlauncher/Launcher.java` — call `OfflineAccountManager.loadAccounts()` at startup.
- `src/main/java/com/atlauncher/mclauncher/MCLauncher.java` — widen account param type to `AbstractAccount`.
- `src/main/java/com/atlauncher/data/Instance.java` — offline account resolution + `instanceof` guards + drop the offline-name prompt.
- `src/main/java/com/atlauncher/constants/UIConstants.java` — new tab constant + switch case.
- `src/main/java/com/atlauncher/gui/LauncherFrame.java` — register the new tab.

> **Design note (deviation from spec):** the spec mentioned an optional `OfflineAccountsViewModel`. To minimize files and coupling, the tab talks directly to `OfflineAccountManager` (no ViewModel). The tab is appended at the **end** of the tab bar (constant value `9`) rather than after Accounts, because tab position equals the constant's int value (`tabs` is a `HashMap<Integer,Tab>` and `App.navigate` calls `setSelectedIndex(constant)`); appending keeps the change purely additive with zero renumbering of existing tabs.

---

### Task 1: `OfflineAccount` data class

**Files:**
- Create: `src/main/java/com/atlauncher/data/OfflineAccount.java`
- Test: `src/test/java/com/atlauncher/data/OfflineAccountTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/atlauncher/data/OfflineAccountTest.java`:

```java
package com.atlauncher.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class OfflineAccountTest {
    @Test
    public void constructorSetsUsernameAndDerivesUuid() {
        OfflineAccount account = new OfflineAccount("Notch");

        assertEquals("Notch", account.username);
        assertEquals("Notch", account.minecraftUsername);
        // stored as 32 hex chars without dashes, matching MicrosoftAccount profile id format
        assertEquals(32, account.uuid.length());
        assertFalse(account.uuid.contains("-"));
    }

    @Test
    public void uuidUsesVanillaOfflineScheme() {
        UUID expected = UUID.nameUUIDFromBytes(
            "OfflinePlayer:Notch".getBytes(StandardCharsets.UTF_8));

        assertEquals(expected, OfflineAccount.offlineUUID("Notch"));
        // getRealUUID() (from AbstractAccount) must reconstruct the same UUID from the stored string
        assertEquals(expected, new OfflineAccount("Notch").getRealUUID());
    }

    @Test
    public void syntheticAuthValues() {
        OfflineAccount account = new OfflineAccount("Notch");

        assertEquals("0", account.getAccessToken());
        assertEquals("0", account.getSessionToken());
        assertEquals("legacy", account.getUserType());
        assertEquals("Notch", account.getCurrentUsername());
        assertNull(account.getSkinUrl());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.atlauncher.data.OfflineAccountTest"`
Expected: FAIL — compilation error, `OfflineAccount` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/com/atlauncher/data/OfflineAccount.java` (keep the existing project license header from another file in `data/`):

```java
package com.atlauncher.data;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * An account used for offline (no-authentication) play. It carries no real
 * credentials: a fixed access/session token of "0" and a UUID derived
 * deterministically from the username (the vanilla offline scheme).
 */
public class OfflineAccount extends AbstractAccount {
    private static final long serialVersionUID = 1L;

    public OfflineAccount(String username) {
        this.username = username;
        this.minecraftUsername = username;
        this.uuid = offlineUUID(username).toString().replace("-", "");
    }

    /**
     * Derives the offline-mode UUID for a username, matching vanilla Minecraft
     * and other launchers: a name-based (version 3) UUID of "OfflinePlayer:<name>".
     */
    public static UUID offlineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getAccessToken() {
        return "0";
    }

    @Override
    public String getSessionToken() {
        return "0";
    }

    @Override
    public String getUserType() {
        return "legacy";
    }

    @Override
    public String getCurrentUsername() {
        return minecraftUsername;
    }

    @Override
    public void updateSkinPreCheck() {
        // no-op: offline accounts have no remote profile
    }

    @Override
    public void changeSkinPreCheck() {
        // no-op: offline accounts have no remote profile
    }

    @Override
    public String getSkinUrl() {
        return null;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.atlauncher.data.OfflineAccountTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/atlauncher/data/OfflineAccount.java src/test/java/com/atlauncher/data/OfflineAccountTest.java
git commit -m "feat: add OfflineAccount synthetic account type"
```

---

### Task 2: Verify flat Gson serialization (no `internalType`)

**Files:**
- Test: `src/test/java/com/atlauncher/data/OfflineAccountTest.java` (add to existing)

This locks in the design decision that a `List<OfflineAccount>` serializes as plain fields (the `AccountTypeAdapter` is registered for the exact `AbstractAccount` type, so it does not fire for a concrete `OfflineAccount` element type).

- [ ] **Step 1: Write the failing test**

Add these imports and test to `OfflineAccountTest.java`:

```java
// add imports
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.Gsons;
import com.google.gson.reflect.TypeToken;
```

```java
    @Test
    public void roundTripsAsFlatJsonWithoutInternalType() {
        List<OfflineAccount> accounts = new ArrayList<>();
        accounts.add(new OfflineAccount("Steve"));

        Type listType = new TypeToken<List<OfflineAccount>>() {}.getType();
        String json = Gsons.DEFAULT.toJson(accounts, listType);

        // AccountTypeAdapter must NOT fire for the concrete OfflineAccount element type
        assertFalse(json.contains("internalType"));

        List<OfflineAccount> back = Gsons.DEFAULT.fromJson(json, listType);
        assertEquals(1, back.size());
        assertEquals("Steve", back.get(0).minecraftUsername);
        assertEquals(new OfflineAccount("Steve").uuid, back.get(0).uuid);
        assertEquals("0", back.get(0).getAccessToken());
    }
```

- [ ] **Step 2: Run test to verify it fails or passes**

Run: `./gradlew test --tests "com.atlauncher.data.OfflineAccountTest"`
Expected: PASS. (If it FAILS because `json.contains("internalType")` is true, the design assumption is wrong — STOP and revisit: the storage must then strip/ignore `internalType`, or the manager must use a concrete-typed adapter. Do not proceed silently.)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/atlauncher/data/OfflineAccountTest.java
git commit -m "test: verify OfflineAccount serializes as flat json"
```

---

### Task 3: Storage path + selected-account setting

**Files:**
- Modify: `src/main/java/com/atlauncher/FileSystem.java:74`
- Modify: `src/main/java/com/atlauncher/data/Settings.java:52`

- [ ] **Step 1: Add the storage path**

In `FileSystem.java`, immediately after the line:

```java
    public static final Path ACCOUNTS = CONFIGS.resolve("accounts.json");
```

add:

```java
    public static final Path OFFLINE_ACCOUNTS = CONFIGS.resolve("offlineaccounts.json");
```

- [ ] **Step 2: Add the selected-account setting**

In `Settings.java`, immediately after the line:

```java
    public String lastAccount;
```

add:

```java
    public String lastOfflineAccount;
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/atlauncher/FileSystem.java src/main/java/com/atlauncher/data/Settings.java
git commit -m "feat: add offlineaccounts.json path and lastOfflineAccount setting"
```

---

### Task 4: `OfflineAccountManager`

**Files:**
- Create: `src/main/java/com/atlauncher/managers/OfflineAccountManager.java`

Mirrors `AccountManager` but for `OfflineAccount`, without the JWT load filter, and tolerant of a null `App.settings` (so it is safe to call from tests/startup).

- [ ] **Step 1: Write the class**

Create `src/main/java/com/atlauncher/managers/OfflineAccountManager.java` (use the project license header):

```java
package com.atlauncher.managers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.OfflineAccount;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class OfflineAccountManager {
    private static final Type offlineAccountListType = new TypeToken<List<OfflineAccount>>() {
    }.getType();

    public static final BehaviorSubject<List<OfflineAccount>> ACCOUNTS = BehaviorSubject
        .createDefault(new ArrayList<>());

    public static final BehaviorSubject<Optional<OfflineAccount>> SELECTED_ACCOUNT = BehaviorSubject
        .createDefault(Optional.empty());

    public static Observable<List<OfflineAccount>> getAccountsObservable() {
        return ACCOUNTS;
    }

    public static Observable<Optional<OfflineAccount>> getSelectedAccountObservable() {
        return SELECTED_ACCOUNT;
    }

    @Nonnull
    public static List<OfflineAccount> getAccounts() {
        return Optional.ofNullable(ACCOUNTS.getValue()).orElse(new ArrayList<>());
    }

    @Nullable
    public static OfflineAccount getSelectedAccount() {
        return SELECTED_ACCOUNT.getValue().orElse(null);
    }

    public static void loadAccounts() {
        LogManager.debug("Loading offline accounts");

        ArrayList<OfflineAccount> newAccounts = new ArrayList<>();

        if (Files.exists(FileSystem.OFFLINE_ACCOUNTS)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                Files.newInputStream(FileSystem.OFFLINE_ACCOUNTS), StandardCharsets.UTF_8)) {
                List<OfflineAccount> accounts = Gsons.DEFAULT.fromJson(fileReader, offlineAccountListType);
                if (accounts != null) {
                    newAccounts.addAll(accounts);
                }
            } catch (Exception e) {
                LogManager.logStackTrace("Exception loading offline accounts", e);
            }
        }

        ACCOUNTS.onNext(newAccounts);

        String lastOfflineAccount = App.settings == null ? null : App.settings.lastOfflineAccount;
        for (OfflineAccount account : newAccounts) {
            if (account.username.equalsIgnoreCase(lastOfflineAccount)) {
                SELECTED_ACCOUNT.onNext(Optional.of(account));
            }
        }

        if (!SELECTED_ACCOUNT.getValue().isPresent() && !newAccounts.isEmpty()) {
            SELECTED_ACCOUNT.onNext(Optional.of(newAccounts.get(0)));
        }

        LogManager.debug("Finished loading offline accounts");
    }

    public static void saveAccounts() {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            Files.newOutputStream(FileSystem.OFFLINE_ACCOUNTS), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(ACCOUNTS.getValue(), offlineAccountListType, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public static void addAccount(OfflineAccount account) {
        LogManager.info("Added Offline Account " + account);

        List<OfflineAccount> accounts = ACCOUNTS.getValue();
        accounts.add(account);
        ACCOUNTS.onNext(accounts);

        switchAccount(account);
        saveAccounts();
    }

    public static void removeAccount(OfflineAccount account) {
        List<OfflineAccount> accounts = ACCOUNTS.getValue();
        if (SELECTED_ACCOUNT.getValue().orElse(null) == account) {
            if (accounts.size() == 1) {
                switchAccount(null);
            } else {
                switchAccount(accounts.get(0) == account ? accounts.get(1) : accounts.get(0));
            }
        }
        accounts.remove(account);
        ACCOUNTS.onNext(accounts);
        saveAccounts();
    }

    public static void switchAccount(@Nullable OfflineAccount account) {
        if (account == null) {
            SELECTED_ACCOUNT.onNext(Optional.empty());
            if (App.settings != null) {
                App.settings.lastOfflineAccount = null;
                App.settings.save();
            }
        } else {
            LogManager.info("Changed offline account to " + account);
            SELECTED_ACCOUNT.onNext(Optional.of(account));
            if (App.settings != null) {
                App.settings.lastOfflineAccount = account.username;
                App.settings.save();
            }
        }
    }

    public static OfflineAccount getAccountByName(String username) {
        for (OfflineAccount account : ACCOUNTS.getValue()) {
            if (account.username.equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    public static boolean isAccountByName(String username) {
        return getAccountByName(username) != null;
    }
}
```

> Note: `LogManager` is `com.atlauncher.managers.LogManager` (same package, no import needed).

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/atlauncher/managers/OfflineAccountManager.java
git commit -m "feat: add OfflineAccountManager with offlineaccounts.json persistence"
```

---

### Task 5: Load offline accounts at startup

**Files:**
- Modify: `src/main/java/com/atlauncher/Launcher.java:127`

- [ ] **Step 1: Add the load call**

In `Launcher.java`, immediately after the line:

```java
        AccountManager.loadAccounts(); // Load the saved Accounts
```

add:

```java
        OfflineAccountManager.loadAccounts(); // Load the saved Offline Accounts
```

Add the import near the other `com.atlauncher.managers.*` imports:

```java
import com.atlauncher.managers.OfflineAccountManager;
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/atlauncher/Launcher.java
git commit -m "feat: load offline accounts at startup"
```

---

### Task 6: Generalize `MCLauncher` to `AbstractAccount`

**Files:**
- Modify: `src/main/java/com/atlauncher/mclauncher/MCLauncher.java`

The launch argument code only calls `getRealUUID()/getAccessToken()/getUserType()/getSessionToken()`, all defined on `AbstractAccount`. Widen the parameter types.

- [ ] **Step 1: Find every `MicrosoftAccount` usage in the file**

Run: `grep -n "MicrosoftAccount" src/main/java/com/atlauncher/mclauncher/MCLauncher.java`
Expected lines: the `import`, and the `account` parameter in `launch` (public, ~55), `launch` (private, ~61), `getArguments` (~163), `replaceArgument` (~466), and `censorArguments` (~493).

- [ ] **Step 2: Replace the type in each signature**

Change the import:

```java
import com.atlauncher.data.MicrosoftAccount;
```
to:
```java
import com.atlauncher.data.AbstractAccount;
```

Then in every method signature listed in Step 1, change the parameter declaration `MicrosoftAccount account` to `AbstractAccount account`. (Do not change any other logic; these methods only use `AbstractAccount` methods.)

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL. If a compile error points to a `MicrosoftAccount`-only member used on `account`, that member must be guarded — but none is expected here.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/atlauncher/mclauncher/MCLauncher.java
git commit -m "refactor: MCLauncher accepts AbstractAccount instead of MicrosoftAccount"
```

---

### Task 7: Offline account resolution in `Instance.launch`

**Files:**
- Modify: `src/main/java/com/atlauncher/data/Instance.java` (`launch(boolean offline)`, ~786-1035)

- [ ] **Step 1: Replace the account-resolution + prompt block**

Replace the block from `final MicrosoftAccount account = ...` through the offline-name prompt and the `final String username = ...` line (currently `Instance.java:791-828`) with:

```java
        final AbstractAccount account;

        if (offline) {
            account = OfflineAccountManager.getSelectedAccount();

            if (account == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Offline Account Selected"))
                    .setContent(new HTMLBuilder().center()
                        .text(GetText.tr(
                            "Cannot play offline as you have no offline account selected. Please add one in the Offline Accounts tab."))
                        .build())
                    .setType(DialogManager.ERROR).show();

                App.navigate(UIConstants.LAUNCHER_OFFLINE_ACCOUNTS_TAB);
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        } else {
            account = launcher.account == null ? AccountManager.getSelectedAccount()
                : AccountManager.getAccountByName(launcher.account);

            if (account == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                    .setContent(new HTMLBuilder().center()
                        .text(GetText.tr("Cannot play instance as you have no account selected.")).build())
                    .setType(DialogManager.ERROR).show();

                if (AccountManager.getAccounts().isEmpty()) {
                    App.navigate(UIConstants.LAUNCHER_ACCOUNTS_TAB);
                }

                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }

        // if Microsoft account must login again, then make sure to do that
        if (!offline && account instanceof MicrosoftAccount && ((MicrosoftAccount) account).mustLogin) {
            if (!((MicrosoftAccount) account).ensureAccountIsLoggedIn()) {
                LogManager.info("You must login to your account before continuing.");
                return false;
            }
        }

        final String username = account.minecraftUsername;
```

- [ ] **Step 2: Fix the online login call type**

Find the `if (!offline)` "Logging into Minecraft" block (currently ~`Instance.java:916-939`). Change the line:

```java
                        loginDialog.setReturnValue(account.ensureAccessTokenValid());
```
to:
```java
                        loginDialog.setReturnValue(((MicrosoftAccount) account).ensureAccessTokenValid());
```

(This is inside the `if (!offline)` branch, where `account` is always a `MicrosoftAccount`.)

- [ ] **Step 3: Guard the access-token log censor for offline**

Find (currently ~`Instance.java:1033`):

```java
                    if (account.getAccessToken() != null) {
                        line = line.replaceAll(account.getAccessToken(), "**ACCESSTOKEN**");
                    }
```
Replace with:
```java
                    if (!offline && account.getAccessToken() != null) {
                        line = line.replaceAll(account.getAccessToken(), "**ACCESSTOKEN**");
                    }
```

(Reason: an offline token is the literal `"0"`; censoring it would replace every `0` in the log output.)

- [ ] **Step 4: Add imports**

Ensure these imports are present in `Instance.java` (add any that are missing):

```java
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.managers.OfflineAccountManager;
```

(`MicrosoftAccount`, `AccountManager`, `UIConstants`, `App`, `HTMLBuilder`, `DialogManager`, `GetText` are already imported and used in this file.)

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Run the existing test suite (regression check)**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (no existing tests broken; `OfflineAccountTest` passes).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/atlauncher/data/Instance.java
git commit -m "feat: resolve offline account from OfflineAccountManager on offline launch"
```

---

### Task 8: Register the Offline Accounts tab constant

**Files:**
- Modify: `src/main/java/com/atlauncher/constants/UIConstants.java`

- [ ] **Step 1: Add the constant**

After the line:

```java
    public static final int LAUNCHER_ABOUT_TAB = 8;
```
add:
```java
    public static final int LAUNCHER_OFFLINE_ACCOUNTS_TAB = 9;
```

- [ ] **Step 2: Add the switch case**

In `getInitialTabName`, after:

```java
            case UIConstants.LAUNCHER_ABOUT_TAB:
                return "About";
```
add:
```java
            case UIConstants.LAUNCHER_OFFLINE_ACCOUNTS_TAB:
                return "Offline Accounts";
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/atlauncher/constants/UIConstants.java
git commit -m "feat: add Offline Accounts tab constant"
```

---

### Task 9: `OfflineAccountsTab` UI

**Files:**
- Create: `src/main/java/com/atlauncher/gui/tabs/offlineaccounts/OfflineAccountsTab.java`
- Modify: `src/main/java/com/atlauncher/gui/LauncherFrame.java` (register the tab)

- [ ] **Step 1: Write the tab**

Create `src/main/java/com/atlauncher/gui/tabs/offlineaccounts/OfflineAccountsTab.java` (use the project license header):

```java
package com.atlauncher.gui.tabs.offlineaccounts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.OfflineAccount;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.OfflineAccountManager;

public class OfflineAccountsTab extends HierarchyPanel implements Tab {
    private static final long serialVersionUID = 1L;

    private JComboBox<OfflineAccount> accountsComboBox;
    private JButton deleteButton;

    public OfflineAccountsTab() {
        super(new BorderLayout());
    }

    @Override
    protected void onShow() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 0, 250));

        JEditorPane infoTextPane = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
            "Offline accounts let you play without a Microsoft account. They cannot connect to "
                + "online (premium) servers. Add an account with a username, then use \"Play Offline\" "
                + "on an instance."))
            .build());
        infoTextPane.setEditable(false);
        infoTextPane.setFocusable(false);
        infoPanel.add(infoTextPane);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        accountsComboBox = new JComboBox<>();
        accountsComboBox.setName("offlineAccountsComboBox");
        accountsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                OfflineAccount selected = (OfflineAccount) accountsComboBox.getSelectedItem();
                if (selected != null) {
                    OfflineAccountManager.switchAccount(selected);
                }
                deleteButton.setVisible(selected != null);
            }
        });
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel buttons = new JPanel(new FlowLayout());

        JButton addButton = new JButton(GetText.tr("Add Offline Account"));
        addButton.addActionListener(e -> onAdd());

        deleteButton = new JButton(GetText.tr("Delete"));
        deleteButton.addActionListener(e -> onDelete());

        buttons.add(addButton);
        buttons.add(deleteButton);
        bottomPanel.add(buttons, gbc);

        add(infoPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);

        reloadAccounts();
    }

    @Override
    protected void createViewModel() {
        // no view model; this tab talks to OfflineAccountManager directly
    }

    @Override
    protected void onDestroy() {
        removeAll();
        accountsComboBox = null;
        deleteButton = null;
    }

    private void reloadAccounts() {
        accountsComboBox.removeAllItems();
        for (OfflineAccount account : OfflineAccountManager.getAccounts()) {
            accountsComboBox.addItem(account);
        }
        OfflineAccount selected = OfflineAccountManager.getSelectedAccount();
        if (selected != null) {
            accountsComboBox.setSelectedItem(selected);
        }
        deleteButton.setVisible(OfflineAccountManager.getSelectedAccount() != null);
    }

    private void onAdd() {
        String name = DialogManager.okDialog().setTitle(GetText.tr("Add Offline Account"))
            .setContent(GetText.tr("Enter a username for the offline account:"))
            .showInput("");

        if (name == null) {
            return; // cancelled
        }
        name = name.trim();

        if (!name.matches("[A-Za-z0-9_]{1,16}")) {
            DialogManager.okDialog().setTitle(GetText.tr("Invalid Username"))
                .setContent(GetText.tr(
                    "Usernames must be 1-16 characters and only contain letters, numbers and underscores."))
                .setType(DialogManager.ERROR).show();
            return;
        }

        if (OfflineAccountManager.isAccountByName(name)) {
            DialogManager.okDialog().setTitle(GetText.tr("Account Exists"))
                .setContent(GetText.tr("An offline account with that username already exists."))
                .setType(DialogManager.ERROR).show();
            return;
        }

        OfflineAccountManager.addAccount(new OfflineAccount(name));
        reloadAccounts();
    }

    private void onDelete() {
        OfflineAccount selected = (OfflineAccount) accountsComboBox.getSelectedItem();
        if (selected == null) {
            return;
        }

        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete"))
            .setContent(GetText.tr("Are you sure you want to delete this offline account?"))
            .setType(DialogManager.WARNING).show();

        if (ret == DialogManager.YES_OPTION) {
            OfflineAccountManager.removeAccount(selected);
            reloadAccounts();
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Offline Accounts");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Offline Accounts";
    }
}
```

> **Important — verify `HierarchyPanel`'s abstract methods before compiling.** Run `grep -n "abstract" src/main/java/com/atlauncher/gui/panels/HierarchyPanel.java`. The overrides above assume `onShow()`, `createViewModel()`, and `onDestroy()`. If the actual abstract method set differs (different names or additional methods), adjust the overrides to match exactly — the names must match `HierarchyPanel`, not be guessed. `OfflineAccount.toString()` returns `minecraftUsername` (inherited from `AbstractAccount`), so the combo shows the username.

- [ ] **Step 2: Register the tab in `LauncherFrame`**

In `LauncherFrame.java`, after the About tab registration block (currently `LauncherFrame.java:215-218`), add:

```java
        PerformanceManager.start("offlineAccountsTab");
        OfflineAccountsTab offlineAccountsTab = new OfflineAccountsTab();
        this.tabs.put(UIConstants.LAUNCHER_OFFLINE_ACCOUNTS_TAB, offlineAccountsTab);
        PerformanceManager.end("offlineAccountsTab");
```

Add the import alongside the other tab imports:

```java
import com.atlauncher.gui.tabs.offlineaccounts.OfflineAccountsTab;
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/atlauncher/gui/tabs/offlineaccounts/OfflineAccountsTab.java src/main/java/com/atlauncher/gui/LauncherFrame.java
git commit -m "feat: add Offline Accounts tab UI"
```

---

### Task 10: End-to-end manual verification

**Files:** none (manual run).

- [ ] **Step 1: Build and run the launcher**

Run: `./gradlew run` (or the project's documented dev run; check `TESTING.md`).
Expected: launcher window opens with a new **"Offline Accounts"** tab at the end of the tab bar.

- [ ] **Step 2: Add an offline account**

In the Offline Accounts tab, click "Add Offline Account", enter `TestPlayer`, confirm.
Expected: the account appears in the combo and is selected; `offlineaccounts.json` is created under the launcher config dir containing `username`/`minecraftUsername`/`uuid` and NO `internalType` field.

- [ ] **Step 3: Validate rejection of bad names**

Click "Add Offline Account", enter `bad name!` (space + punctuation), confirm.
Expected: "Invalid Username" error; no account added.

- [ ] **Step 4: Launch an instance offline with no Microsoft account**

With no Microsoft account selected (or none added), on any installed instance choose Play ▸ **Play Offline**.
Expected: no "No Account Selected" error; Minecraft launches in offline mode using `TestPlayer`. The launch log shows `--accessToken 0` style args and no auth network step. (If no instance is installed, install a small vanilla pack first.)

- [ ] **Step 5: Confirm online play still works**

If a Microsoft account is available, select it and use the default **Play** button.
Expected: normal online login + launch, unchanged from before.

- [ ] **Step 6: Confirm persistence across restart**

Close and reopen the launcher.
Expected: the `TestPlayer` offline account is still listed and selected in the Offline Accounts tab.

- [ ] **Step 7: Commit any fixes** made during verification, then the feature branch is ready for review/merge.

---

## Self-Review

**Spec coverage:**
- `OfflineAccount` type (token "0", UUID from name) → Task 1. ✓
- Flat serialization, no `AccountTypeAdapter` → Task 2. ✓
- `OfflineAccountManager` + `offlineaccounts.json`, no JWT filter, persisted selection → Tasks 3, 4. ✓
- Load at startup → Task 5. ✓
- Launch pipeline `MicrosoftAccount` → `AbstractAccount` + `instanceof` guards → Tasks 6, 7. ✓
- Offline account resolution / removal of name prompt → Task 7. ✓
- Offline Accounts tab + registration + name validation → Tasks 8, 9. ✓
- Error handling (no offline account selected → dialog + navigate) → Task 7 Step 1. ✓
- Out-of-scope items (skins, default-account unification) intentionally not implemented. ✓

**Placeholder scan:** No TBD/TODO; all code shown in full. The only deliberately verify-before-write item is `HierarchyPanel`'s abstract method set (Task 9 Step 1 note) — flagged explicitly rather than guessed.

**Type consistency:** `OfflineAccount(String)`, `OfflineAccount.offlineUUID(String)`, `OfflineAccountManager.getSelectedAccount()/getAccounts()/addAccount(OfflineAccount)/removeAccount(OfflineAccount)/switchAccount(OfflineAccount)/isAccountByName(String)`, `UIConstants.LAUNCHER_OFFLINE_ACCOUNTS_TAB`, and the `AbstractAccount account` parameter types are used consistently across tasks.
