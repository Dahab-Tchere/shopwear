require('dotenv').config();
const express = require('express');
const { MongoClient } = require('mongodb');

const app = express();
const port = process.env.PORT || 3000;

async function connectDB() {
  const uri = process.env.MONGODB_URI;
  if (!uri) {
    throw new Error('MONGODB_URI is not defined in .env file');
  }
  const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });
  try {
    await client.connect();
    console.log('Connected to MongoDB');
    return client.db('myNewDatabase');
  } catch (error) {
    console.error('Error connecting to MongoDB:', error);
    throw error;
  }
}

app.use(express.json());

app.get('/', async (req, res) => {
  try {
    const db = await connectDB();
    const collection = db.collection('myCollection');
    await collection.insertOne({ test: 'Hello, MongoDB!' });
    const documents = await collection.find({}).toArray();
    res.json({ message: 'Document inserted', documents });
  } catch (error) {
    res.status(500).json({ error: 'Failed to connect to MongoDB' });
  }
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});