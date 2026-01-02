const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const admin = require("firebase-admin");

var serviceAccount = require("./service-account.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

exports.notifyAdminsOnNewEvent = onDocumentCreated("events/{eventId}", (event) => {
    if (!event.data) return;
    const newEvent = event.data.data();
    console.log("Nuevo evento detectado:", newEvent);
    const message = {
        notification: {
            title: "Nuevo Evento Pendiente",
            body: `El usuario ${newEvent.userEmail} ha creado: ${newEvent.title}`
        },
        topic: "admin_alerts"
    };

    return getMessaging().send(message)
        .then((response) => {
            console.log("Notificación enviada exitosamente:", response);
        })
        .catch((error) => {
            console.log("Error enviando notificación:", error);
        });
});

exports.sendPushNotification = onCall(async (request) => {

    if (!request.auth) {
        throw new HttpsError('unauthenticated', 'User must be logged in');
    }

    const { tokens, topic, title, body } = request.data;
    const messagePayload = {
        notification: {
            title: title || "Aviso Importante",
            body: body || "Tienes una nueva notificación."
        }
    };

    try {
        if (topic) {
            messagePayload.topic = topic;
            await getMessaging().send(messagePayload);
            return { success: true, method: "topic" };
        }
        if (tokens && tokens.length > 0) {
            messagePayload.tokens = tokens;
            const response = await getMessaging().sendEachForMulticast(messagePayload);
            return { success: true, failureCount: response.failureCount, method: "tokens" };
        }
        return { success: false, error: "No destination (tokens or topic) provided" };
    } catch (error) {
        console.error("Error sending notification:", error);
        throw new HttpsError('internal', error.message);
    }
});

exports.notifyUserOnEventResolution = onDocumentUpdated("events/{eventId}", async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();
    if (before.status === after.status) return;

    if (after.status !== "APPROVED" && after.status !== "REJECTED") return;

    const userId = after.userId;
    console.log(`El evento ${event.params.eventId} cambió a ${after.status}. Notificando a usuario ${userId}...`);

    try {
        const userSnapshot = await admin.firestore().collection("users").doc(userId).get();
        const userData = userSnapshot.data();

        if (!userData || !userData.fcmToken) {
            console.log("El usuario no tiene token FCM registrado. No se puede enviar notificación.");
            return;
        }

        const isApproved = after.status === "APPROVED";
        const emoji = isApproved ? "✅" : "❌";
        const title = `${emoji} Evento ${isApproved ? "Aprobado" : "Rechazado"}`;

        let bodyMsg = `Tu evento "${after.title}" ha sido revisado.`;
        if (after.adminFeedback) {
            bodyMsg += `\nComentario: ${after.adminFeedback}`;
        }

        await admin.firestore()
            .collection("users")
            .doc(userId)
            .collection("notifications")
            .add({
                title: title,
                message: bodyMsg,
                date: admin.firestore.FieldValue.serverTimestamp(),
                read: false,
                type: isApproved ? "SUCCESS" : "ERROR"
            });

        const message = {
            notification: {
                title: title,
                body: bodyMsg
            },
            token: userData.fcmToken // Enviar directo al dispositivo del usuario
        };

        await getMessaging().send(message);
        console.log("Notificación de resolución enviada con éxito.");

    } catch (error) {
        console.error("Error en notifyUserOnEventResolution:", error);
    }
});