const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');

const app = express();
app.use(cors());

const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST'],
  },
});

const users = new Map();

io.on('connection', (socket) => {
  console.log('New client connected:', socket.id);

  socket.on('join', ({ userId, otherUserId }) => {
    users.set(userId, socket.id);
    socket.userId = userId;
    console.log(`User ${userId} joined`);

    socket.emit('joined', { success: true });
  });

  socket.on('offer', ({ offer, otherUserId }) => {
    const targetSocketId = users.get(otherUserId);
    if (targetSocketId) {
      io.to(targetSocketId).emit('offer', { offer, otherUserId: socket.userId });
      console.log(`Offer sent from ${socket.userId} to ${otherUserId}`);
    } else {
      console.log(`Target user ${otherUserId} not found`);
    }
  });

  socket.on('answer', ({ answer, otherUserId }) => {
    const targetSocketId = users.get(otherUserId);
    if (targetSocketId) {
      io.to(targetSocketId).emit('answer', { answer, otherUserId: socket.userId });
      console.log(`Answer sent from ${socket.userId} to ${otherUserId}`);
    }
  });

  socket.on('candidate', ({ candidate, otherUserId }) => {
    const targetSocketId = users.get(otherUserId);
    if (targetSocketId) {
      io.to(targetSocketId).emit('candidate', { candidate, otherUserId: socket.userId });
      console.log(`Candidate sent from ${socket.userId} to ${otherUserId}`);
    } else {
      console.log(`Candidate target ${otherUserId} not found`);
    }
  });

  socket.on('disconnect', () => {
    console.log('Client disconnected:', socket.userId);
    if (socket.userId) {
      users.delete(socket.userId);
    }
  });
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
