let editMarker,
    map,
    displayMarkers = {},
    currentImage,
    currentActiveInfoWindow;

/**
 * Adds a new marker with a toggable info window
 * @param {Number} lat 
 * @param {Number} lng 
 * @param {String} description 
 */
function addDisplayMarker(lat, lng, title, image){
  const marker = new google.maps.Marker({
    position: { lat: lat, lng: lng },
    map: map
  })
  const infoWindow = new google.maps.InfoWindow({
    content: buildDeletableMarker( lat, lng, title)
  })
  console.log('infoWindow1 :', infoWindow);
  marker.addListener('click', () => {
    showLandmark(image, infoWindow, marker);
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
 */
function createMap(){
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.764419, lng: -122.445911},
    zoom: 12
  });
  map.addListener('click', (event) => {
    createMarkerForEdit(event.latLng.lat(), event.latLng.lng());
  })
  // document.getElementById('btnSubmit').addEventListener('click',postMarker)
  fetchMarkers();
}

 /** Fetches markers from the backend and adds them to the map. */
function fetchMarkers(){
  fetch('/markers')
  .then((response) => response.json())
  .then((markers) => {
    console.log('markers :', markers);
    markers.forEach((marker) => {
     addDisplayMarker(marker.lat, marker.lng, marker.content, marker.landmark)
    });
  });
}

/** Creates a marker that shows a textbox the user can edit. */
function createMarkerForEdit(lat, lng){
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
  let lat = editMarker.getPosition().lat();
  let lng = editMarker.getPosition().lng();
  let content = document.getElementById('title-input').value;
  let landmark = document.getElementById('landmark-input').files[0];
  // Get blobstore upload url
  fetch('/markers/uploadURL')
  .then(response => response.text())
  .then(uploadURL => {

    let h = new Headers();
    h.append('Accept', 'application/json');
    let fd = new FormData();
    fd.append('lat', lat);
    fd.append('lng', lng);
    fd.append('content', content);
    fd.append('landmark',landmark);
  
    // Create request to upload url
    let req = new Request(uploadURL,{
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
    let {infoWindow, mapMarker} = addDisplayMarker(marker.lat, marker.lng, marker.content, marker.landmark);
    showLandmark(marker.landmark, infoWindow, mapMarker);
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

function showLandmark(image, infoWindow, marker){
  closeOtherDialogs();
  console.log('infoWindow :', infoWindow);
  if(currentImage){
    // If there is a previous image displayed, remove it
    currentImage.src = image;
    
  } else {
    // Show current image'
    let div = document.getElementById('landmark-div');
    currentImage = document.createElement('img')
    currentImage.src = image;
    div.appendChild(currentImage);
  }
  infoWindow.open(map, marker);
  currentActiveInfoWindow = infoWindow;
}
