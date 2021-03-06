let editMarker,
    map,
    displayMarkers = {},
    landmarkDiv,
    currentLandmark = {},
    currentActiveInfoWindow;
/**
 * Adds a new marker with a toggable info window
 * @param {Number} lat 
 * @param {Number} lng 
 * @param {String} description 
 */
function addDisplayMarker(lat, lng, title, image, ratingsArray){
  const marker = new google.maps.Marker({
    position: { lat: lat, lng: lng },
    map: map
  })
  const infoWindow = new google.maps.InfoWindow({
    content: buildDeletableMarker( lat, lng, title)
  })
  let ratings = calculateRatingAvg(ratingsArray);

  marker.addListener('click', () => {
    showLandmark(image, infoWindow, marker, ratings);
    currentLandmark.lat = lat;
    currentLandmark.lng = lng;
    currentLandmark.content = title;
  })

  // Uses a dictionary with both lat and lng for faster lookup
  if(!displayMarkers[lat]){
    displayMarkers[lat] = {};
  } 
  displayMarkers[lat][lng] = marker;
  return { infoWindow, marker };
}

/**
 * Main function
 * @param {Number} lat 
 * @param {Number} lng 
 */
function createMap(lat, lng){
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: lat, lng: lng},
    zoom: 12
  });
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  })
  fetchMarkers();
  configureStars();
}

 /** Fetches markers from the backend and adds them to the map. */
function fetchMarkers(){
  fetch('/markers')
  .then((response) => response.json())
  .then((markers) => {
    markers.forEach((marker,i) => {
     addDisplayMarker(marker.lat, marker.lng, marker.content, marker.landmark, marker.ratings);
    });
  });
}

/** Creates a marker that shows a textbox the user can edit. */
function createMarkerForEdit(lat, lng){
  checkLoginStatus()
    .then(username => {
      // If we're already showing an editable marker, then remove it.
      closeOtherDialogs();
      
      editMarker = new google.maps.Marker({
        position: {lat: lat, lng: lng},
        map: map
      });
      const infoWindow = new google.maps.InfoWindow({
        content: buildInfoWindowInput()
      });
      // When the user closes the editable info window, remove the marker.
      google.maps.event.addListener(infoWindow, 'closeclick', () => {
        editMarker.setMap(null);
      });
      infoWindow.open(map, editMarker);
    }) 
    .catch(err => {
      // Alerts for now, maybe create another type of pop up
      // message that is nicer in future versions?
      alert(err)
      window.location.href= '/login'
    })
}

/** Builds and returns HTML elements that show an editable textbox and a submit button. */
function buildInfoWindowInput(){
  
  const markerForm = document.getElementById('marker-form').cloneNode(true);
  const containerDiv = document.createElement('div');
  containerDiv.appendChild(markerForm);
  markerForm.classList.remove('hidden');
  return containerDiv;
}


/** Sends a marker to the backend for saving. */
function postMarker(ev){
  // Don't let form be submitted
  ev.preventDefault();
  // Get all info from marker and form
  const lat = editMarker.getPosition().lat();
  const lng = editMarker.getPosition().lng();
  const content = document.getElementById('title-input').value;
  const landmark = document.getElementById('landmark-input').files[0];
  // Get blobstore upload url
  fetch('/markers/uploadURL')
  .then(response => response.text())
  .then(uploadURL => {

    const h = new Headers();
    h.append('Accept', 'application/json');
    const fd = new FormData();
    fd.append('lat', lat);
    fd.append('lng', lng);
    fd.append('content', content);
    fd.append('landmark',landmark);
  
    // Create request to upload url
    const req = new Request(uploadURL,{
      method: 'POST',
      headers: h,
      body: fd
    })

    return fetch(req)
  })
  .then( response => response.json())
  .then(marker => {

    // Create display marker for this newly created landmark
    editMarker.setMap(null);
    const {infoWindow, mapMarker} = addDisplayMarker(marker.lat, marker.lng, marker.content, marker.landmark, marker.ratings);
    let ratingObj = calculateRatingAvg(marker.ratings); 
    showLandmark(marker.landmark, infoWindow, mapMarker,  ratingObj);
    infoWindow.open(map, mapMarker);
  })
  // Future integrations: Create dialog box with errors
  .catch(err => console.log(err))
}

function buildDeletableMarker(lat, lng, content){ 
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Delete Marker'));
  button.onclick =() => {
    removeMarker(lat,lng, content);
  }
  const containerDiv = document.createElement('div');
  containerDiv.appendChild(document.createTextNode(content))
  containerDiv.appendChild(document.createElement('br'))
  containerDiv.appendChild(button)

  return containerDiv;
}

function removeMarker(lat, lng, content){
  const baseURL = window.location.protocol + '//' + window.location.host;  
  const url = new URL(baseURL+'/markers');
  url.searchParams.append('lat',lat);
  url.searchParams.append('lng',lng);
  url.searchParams.append('content',content);
  
  // Removes marker from datastore
  fetch(url, {
    method:'DELETE'
  })
  .then(response => response.json())
  .then( marker => {
    // Finds marker after being deleted from datastore,
    //  removes it from map, then from dictionary
    displayMarkers[lat][lng].setMap(null);
    currentImage.parentNode.removeChild(currentImage);
    currentImage = null;
    delete displayMarkers[lat][lng];
  })
  .catch(error => console.log(error));
}

function closeOtherDialogs(){
  if(editMarker){
    editMarker.setMap(null);
  }
  if(currentActiveInfoWindow){
    currentActiveInfoWindow.close();
  }
}

function showLandmark(image, infoWindow, marker, ratings){
  closeOtherDialogs();

  if(!landmarkDiv){
    landmarkDiv = document.getElementById('landmark-div');
    landmarkDiv.classList.remove('hidden');
  }
  // Display image
  landmarkDiv.children[0].src = image;
  
  // Display ratings if available
  if(ratings){
    displayRating(ratings.avg);
    landmarkDiv.children[2].innerText = `Total ratings: ${ratings.total}` ;
  } else {
    landmarkDiv.children[2].innerText = 'Ratings Unavailable';
  }

  if(infoWindow){
    infoWindow.open(map, marker);
    currentActiveInfoWindow = infoWindow;
  }
  currentLandmark.avg = ratings.avg;
}

function displayRating(average){

  let roundAvg = Math.round(average);
  let rateDiv = document.getElementById('ratings');
  for(let i=0; i<5; i++){
    if((i+1)<=roundAvg){
      rateDiv.children[i].innerText = 'star';
    } else {
      rateDiv.children[i].innerText = 'star_border';
    }
  }
}

function configureStars(){
  let ratingsDiv = document.getElementById('ratings');
  for (let i = 0; i<5; i++) {
    ratingsDiv.children[i].addEventListener('click', ()=> {
      checkLoginStatus()
      .then(username =>{
        const baseURL = window.location.protocol + '//' + window.location.host;  
        const url = new URL(baseURL+'/ratings');
        url.searchParams.append('lat',currentLandmark.lat);
        url.searchParams.append('lng',currentLandmark.lng);
        url.searchParams.append('content',currentLandmark.content);
        url.searchParams.append('rating',(i+1));
        // Create request to upload url
        fetch(url,{
          method: 'POST'
        })
        .then(response => response.json())
        .then(newMarker => {
          let ratingsObj = calculateRatingAvg(newMarker.ratings);
          showLandmark(newMarker.landmark,null, null, ratingsObj);
        })
      })
      .catch(err =>{
        alert(err)
        window.location.href= '/login'
  
      })
    })
    ratingsDiv.children[i].addEventListener('mouseover', () => {
      for(let j=0; j<5; j++){
        if(j<=i){
          ratingsDiv.children[j].innerText = 'star';
        } else {
          ratingsDiv.children[j].innerText = 'star_border';
        }
      }
    })    
    ratingsDiv.children[i].addEventListener('mouseleave', () => {
      displayRating(currentLandmark.avg);
    })    
  }
}

function calculateRatingAvg(ratingsArray) {
  let ratings;
  if(ratingsArray){
    let avg = 0;
    let total = 0;
    for(let i=0; i<5; i++){
      // sums all weighted
      avg += ratingsArray[i]*(i+1);
      // Counts Ratings
      total += ratingsArray[i];
    }
    // Calculates avg
    avg /= total;

    ratings = {avg, total};
  }
  return ratings;
}

function checkLoginStatus(){
  return new Promise((resolve, reject) => {
    fetch('/login-status')
      .then(response => response.json())
      .then(loginStatus => {
        if(loginStatus.isLoggedIn){
          return resolve(loginStatus.username);
        } else {
          return reject("You need to be logged in to do that");
        }
      })
  });
}