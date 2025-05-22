Shopwear – Node.js + Express + MongoDB Application
This is a simple Node.js and Express web application that connects to a MongoDB database. It allows inserting and retrieving documents — ideal for small e-commerce, logging, or testing cloud DB connections.
Technologies Used
- Node.js
- Express.js
- MongoDB (via MongoDB Atlas)
- Mongoose
- Heroku
- Git & GitHub
Features
- Connects to MongoDB Atlas (cloud-hosted)
- REST API for inserting and retrieving test data
- Uses `.env` for secure configuration
- Deployed live on Heroku
Folder Structure
shopwear/
│
├── index.js           # Main server file
├── .env               # Environment variables
├── Procfile           # Heroku start command
├── package.json       # Node dependencies
└── README.md          # Project documentation
Installation
1. Clone the repository:
git clone https://github.com/your-username/shopwear.git
cd shopwear

2. Install dependencies:
npm install

3. Create a `.env` file and add:
MONGODB_URI=your-mongodb-atlas-uri
PORT=3000
Running Locally
node index.js
Visit http://localhost:3000
Deployment (Heroku)
1. Login to Heroku:
heroku login

2. Create Heroku app:
heroku create shopwear-tchere

3. Set MongoDB URI:
heroku config:set MONGODB_URI=your-mongodb-atlas-uri

4. Push code:
git push heroku main

5. Open app:
heroku open
Live Demo
https://shopwear-tchere.herokuapp.com
API Endpoints
POST /insert
Inserts a sample document:
{
  "test": "Hello, MongoDB!"
}

GET /data
Returns all inserted documents from the database.
Author
Name: Abbadahab Ahmat Tchere
University: Adventist University of Central Africa (AUCA)
Notes
This project is part of a class assignment and deployed for demonstration purposes.
Feel free to clone and build on top of this template.
