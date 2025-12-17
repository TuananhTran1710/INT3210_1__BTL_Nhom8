/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

// Thiết lập cấu hình chung (Ví dụ: RAM, Region)
setGlobalOptions({ maxInstances: 10 });

exports.sendChatNotification = onDocumentCreated("chats/{chatId}/messages/{messageId}", async (event) => {
    // --- LẤY DỮ LIỆU TỪ EVENT (Cú pháp v2) ---
    // 1. Lấy dữ liệu document vừa tạo
    const snapshot = event.data;
    if (!snapshot) {
        logger.log("No data associated with the event");
        return;
    }
    const message = snapshot.data();

    // 2. Lấy tham số từ URL (chatId)
    const chatId = event.params.chatId;
    const receiverId = message.receiverId; 
    const senderId = message.senderId;

    // --- LOGIC GỬI THÔNG BÁO (Giữ nguyên logic cũ) ---

    // 1. Lấy token của người nhận
    const userDoc = await admin.firestore().collection("users").doc(receiverId).get();
    
    if (!userDoc.exists) {
        logger.log("No user found with ID: " + receiverId);
        return;
    }

    const userData = userDoc.data();
    const fcmToken = userData.fcmToken;

    if (!fcmToken) {
      logger.log("No token for user: " + receiverId);
      return;
    }

    // 2. Lấy tên người gửi
    const senderDoc = await admin.firestore().collection("users").doc(senderId).get();
    let senderName = "Ai đó";
    if (senderDoc.exists) {
        senderName = senderDoc.data().username || "Ai đó";
    }

    // 3. Tạo nội dung thông báo
    let bodyText = message.content;
    
    if (message.mediaUrl && Array.isArray(message.mediaUrl) && message.mediaUrl.length > 0) {
        bodyText = "Đã gửi một ảnh 📷";
    }

    const payload = {
      token: fcmToken,
      data: {
        title: senderName,
        body: bodyText,
        chatId: chatId,
      },
      notification: {
          title: senderName,
          body: bodyText,
      },
      android: {
          priority: "high",
          notification: {
              sound: "default",
              channelId: "wink_chat_channel"
          }
      }
    };

    // 4. Gửi
    try {
        const response = await admin.messaging().send(payload);
        logger.log("Successfully sent message:", response);
        return response;
    } catch (error) {
        logger.error("Error sending message:", error);
        return null;
    }
});