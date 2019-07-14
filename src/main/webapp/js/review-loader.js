// Fetch messages and add them to the page.
function fetchMessages(city){
  const url = '/feed';
  let header = new Headers();
  header.append('city', city);

  fetch(url, {
    method: 'GET',
    headers: header
  }).then(response => response.json())
  .then((messages) => {
    const reviewsContainer = document.getElementById('reviews-container');
    if(messages.length == 0){
     reviewsContainer.innerHTML = '<p>There are no reviews yet.</p>';
    }
    messages.forEach((message) => {
     const reviewDiv = buildMessageDiv(message);
     reviewsContainer.appendChild(reviewDiv);
    });
  });
}

function buildMessageDiv(message){
 const header = document.createElement('h4');
 const reviewerURL = document.createElement('a');
 const url = "/user-page.html?user=" + message.user;
 reviewerURL.href = url;
 reviewerURL.appendChild(document.createTextNode(message.user));
 reviewerURL.classList.add('reviewer');
 header.appendChild(reviewerURL);

 const bodyDiv = document.createElement('div');
 bodyDiv.classList.add('message-body');
 // If user doesn't attach image file, dont' include the imageUrl in the message body.
 if (message.imageUrl == '<img src="null" />') {
    bodyDiv.innerHTML = message.text;
 }
 else {
    bodyDiv.innerHTML = message.text + message.imageUrl;
 }
 const reviewDiv = document.createElement('div');
 reviewDiv.classList.add("card-body");
 reviewDiv.appendChild(header);
 reviewDiv.appendChild(bodyDiv);

 return reviewDiv;
}

// Fetch data and populate the UI of the page.
function buildUI(city){
 fetchMessages(city);
}
