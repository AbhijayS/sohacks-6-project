const express = require('express');
const app = express();
const port = 3000;
const multer = require('multer');
const path = require('path');

var storage = multer.diskStorage({
  destination: function (req, file, cb) {
	cb(null, 'public/')
  },
  filename: function (req, file, cb) {
	cb(null, "data.csv");
  }
})

var upload = multer({storage: storage});

app.use(express.static('public'))

app.listen(port, () => console.log(`app listening on port ${port}`));

app.get('/', function (req, res) {
	res.sendFile(path.join(__dirname + '/public/test.html'));
});

app.post('/', upload.single("file"), function (req, res) {
	console.log(req.file);
	res.redirect('/');
})