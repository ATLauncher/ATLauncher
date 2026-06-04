# Offline Accounts (accountless offline mode) — Design

**Date:** 2026-06-04
**Status:** Approved design, pending implementation plan

## Goal

Let ATLauncher launch Minecraft in offline mode with **no Microsoft account required**, by
introducing a self-contained offline-account subsystem. The reference for behaviour is
PrismLauncher-Cracked's offline flow (synthetic account with token `"0"`, UUID derived from the
username, no auth network calls).

A hard requirement is **upstream-merge friendliness**: nearly all new logic lives in new files,
and edits to existing upstream files are small, localized, and isolated from the Microsoft-account
UI. Offline accounts live in their **own tab** and **own storage file**, never mixed with the
Microsoft accounts subsystem.

## Background (current state)

- ATLauncher already has partial offline support: `Instance.launch(boolean offline)`, a "Play
  Offline" menu item in `InstanceCard`, an offline-name prompt, and it skips the Microsoft token
  refresh when `offline`.
- The blocker: the whole launch pipeline is hard-typed to `MicrosoftAccount`, and
  `Instance.launch` aborts at `if (account == null)` even for offline launches. There is no offline
  account type, so "Play Offline" still requires a selected Microsoft account.
- `AbstractAccount` (base) and `AccountTypeAdapter` (polymorphic serialization via `internalType`)
  exist, but `AccountManager`, its storage (`accounts.json`), and the launch path are all typed to
  `MicrosoftAccount`. `AccountManager.loadAccounts()` additionally filters out any account whose
  `accessToken` is not a 3-part JWT — so an offline account stored in `accounts.json` would be
  discarded on load. This is why offline accounts need their own manager and storage.

## Reference mapping (Prism → ATLauncher)

| Prism piece | ATLauncher equivalent |
|---|---|
| `MinecraftAccount::createOffline()` (synthetic account, token "0", UUID from name) | new `OfflineAccount extends AbstractAccount` |
| `LaunchController::decideAccount()` (bypass account gate) | offline branch of `Instance.launch` resolves account from `OfflineAccountManager` instead of aborting |
| `AuthSession::MakeOffline()` + arg injection | `MCLauncher.replaceArgument` already injects `${auth_*}`/`${user_type}` via `account.getX()`; works once signatures accept `AbstractAccount` |
| `AccountListPage::on_actionAddOffline` (UI) | new `OfflineAccountsTab` |

## Chosen approach

**Separate `OfflineAccountManager` + dedicated "Offline Accounts" tab, with the launch pipeline
generalized from `MicrosoftAccount` to `AbstractAccount`.** Microsoft-specific calls are guarded
with `instanceof MicrosoftAccount`.

Rejected alternatives:
- Reuse `AccountManager`/`accounts.json` for both types — the JWT load filter discards offline
  accounts, it mixes UI, and it maximizes merge conflicts with upstream.
- Ephemeral-only offline launch (no persistence/manager) — rejected; persistence + a tab were
  required.

## Components

| Component | Kind | Responsibility |
|---|---|---|
| `data/OfflineAccount.java` | new, `extends AbstractAccount` | Synthetic account. `getAccessToken()`→`"0"`, `getSessionToken()`→`"0"`, `getUserType()`→`"legacy"`, `getCurrentUsername()`→`minecraftUsername`, `getSkinUrl()`→`null`, `updateSkinPreCheck()`/`changeSkinPreCheck()`→no-op. UUID set in constructor via vanilla offline scheme: `UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(UTF_8))`. Stores `username`/`minecraftUsername`/`uuid` (inherited). |
| `managers/OfflineAccountManager.java` | new, parallel to `AccountManager` | RxJava `BehaviorSubject<List<OfflineAccount>>` + `BehaviorSubject<Optional<OfflineAccount>>` selected; `loadAccounts/saveAccounts/addAccount/removeAccount/switchAccount/getSelectedAccount/getAccountByName`. No JWT filter. |
| `FileSystem.OFFLINE_ACCOUNTS` | 1-line edit | `CONFIGS.resolve("offlineaccounts.json")`. |
| `gui/tabs/offlineaccounts/OfflineAccountsTab.java` (+ `OfflineAccountsViewModel`) | new | Minimal tab: combo of offline accounts, "Add Offline Account" (name prompt), "Delete", select-to-activate. No login, no skin management. Extends `HierarchyPanel implements Tab`. |
| `constants/UIConstants` + `gui/LauncherFrame.java` | small edit | New `LAUNCHER_OFFLINE_ACCOUNTS_TAB`; register `new OfflineAccountsTab()` after `accountsTab` (around `LauncherFrame.java:201-203`). |
| `App` startup | 1-line edit | Call `OfflineAccountManager.loadAccounts()` alongside `AccountManager.loadAccounts()`. |
| `data/Instance.java` (`launch`) + `mclauncher/MCLauncher.java` (`launch`/`getArguments`/`replaceArgument`/`censorArguments`) | signature edits | `MicrosoftAccount` → `AbstractAccount`; guard MS-specific calls with `instanceof MicrosoftAccount`. |

## Launch data flow (`Instance.launch(boolean offline)`)

```
AbstractAccount account
if (offline):
    account = OfflineAccountManager.getSelectedAccount()
    if (account == null):
        dialog "No offline account selected" + navigate to Offline Accounts tab
        return false
else:  // unchanged online path
    account = (launcher.account == null) ? AccountManager.getSelectedAccount()
                                         : AccountManager.getAccountByName(launcher.account)
    if (account == null): existing "No Account Selected" dialog + return false
```

- MS-only calls (`mustLogin`, `ensureAccountIsLoggedIn()` at `Instance.java:809`;
  `ensureAccessTokenValid()` block at `Instance.java:916-939`) stay under their existing
  `if (!offline ...)` guards, additionally narrowed with `account instanceof MicrosoftAccount`.
- The current offline-name prompt (`Instance.java:818-826`) is **removed**; the name comes from
  the selected `OfflineAccount`, so `username = account.minecraftUsername`.
- Argument assembly (`MCLauncher`) is unchanged in logic — only parameter types widen to
  `AbstractAccount`. It only calls `getRealUUID()/getAccessToken()/getUserType()/getSessionToken()`,
  all defined on `AbstractAccount`.
- Log censoring (`Instance.java:993-1034`) operates on `account.uuid/minecraftUsername/username/
  getAccessToken()`; for offline `username == minecraftUsername` and token `"0"`, so it works
  unchanged.

## Persistence & serialization

- `offlineaccounts.json` is independent from `accounts.json`, serialized with `Gsons.DEFAULT` as a
  homogeneous `List<OfflineAccount>`. Because the concrete type is known on read and write, the
  `AccountTypeAdapter`/`internalType` mechanism is **not** used — Gson serializes the flat fields
  (`username`, `minecraftUsername`, `uuid`).
- Selected offline account is persisted by name in a dedicated setting (e.g.
  `App.settings.lastOfflineAccount`); on load, restore by name, else default to the first entry.
- No JWT filtering on load (that check is Microsoft-specific).

## Offline Accounts tab behaviour

- Combo lists existing offline accounts (display name; default head/skin acceptable).
- "Add Offline Account" → text dialog for the name → `new OfflineAccount(name)` →
  `OfflineAccountManager.addAccount(...)`.
- "Delete" removes the selected account.
- Selecting in the combo calls `switchAccount(...)`, setting the active offline account used by
  "Play Offline".
- Name validation: Minecraft offline usernames are 1–16 chars matching `[A-Za-z0-9_]`; the Add
  dialog rejects invalid names and duplicates (case-insensitive).

## Error handling

Offline launching performs no auth network calls, so the only new failure point is "no offline
account selected", handled with a dialog plus redirection to the Offline Accounts tab. All other
launch steps (RAM/permgen warnings, prepare, process creation, log handling) are identical to the
existing flow.

## Out of scope (YAGNI)

- Skin management / custom skins for offline accounts (use default skin).
- Importing/migrating existing Microsoft accounts into offline accounts.
- Making an offline account selectable as the global default for the online "Play" button
  (online and offline remain independent, symmetric flows).
- Per-instance offline account override beyond the existing `launcher.account` mechanism.

## Files touched (summary)

New: `data/OfflineAccount.java`, `managers/OfflineAccountManager.java`,
`gui/tabs/offlineaccounts/OfflineAccountsTab.java`, `viewmodel/.../OfflineAccountsViewModel.java`
(+ base interface if mirroring `IAccountsViewModel`).

Edited (small/localized): `FileSystem`, `constants/UIConstants`, `gui/LauncherFrame.java`,
`App` startup, `data/Instance.java` (`launch`), `mclauncher/MCLauncher.java` (signatures),
settings class (new `lastOfflineAccount`).
