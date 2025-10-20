# Push notification extensions

New gameplay events can be introduced by sending a POST request to `/api/push/send` with the
following fields:

| Field | Description |
| ----- | ----------- |
| `userId` | Target user identifier registered via `/api/devices`. |
| `type` | Short machine readable event name such as `match_start` or `deposit_success`. |
| `title` | Optional notification title for systems that display a notification fallback. |
| `body` | Optional notification body string. |
| `metadata[...]` | Additional key/value pairs delivered to the Android client in the FCM data payload. |

The Android client consumes the `type`, `title`, `body` and `metadata` keys inside
`LudoFcmService.onMessageReceived`. Add new behaviours by updating that method with the desired
UI action and by ensuring the backend populates any metadata needed by the view layer.

Telemetry for each push lives in `backend/src/admin/message-telemetry.log`, which can be tailed to
validate new message types during rollout.
