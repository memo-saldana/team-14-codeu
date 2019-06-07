 /**
 * Adds a new marker with a toggable info window
 * @param {Map} map 
 * @param {Number} lat 
 * @param {Number} lng 
 * @param {String} title 
 * @param {String} description 
 */
function addLandmark(map, lat, lng, title, description){
  const marker = new google.maps.Marker({
    position: { lat: lat, lng: lng },
    map: map,
    title: title
  })
  const infoWindow = new google.maps.InfoWindow({
    content: description
  })
  marker.addListener('click', () => {
    infoWindow.open(map, marker)
  })
}

/**
 * Main function
 */
function createMap(){
  const map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.764419, lng: -122.445911},
    zoom: 12
  });

  addLandmark(map, 37.819021, -122.477946, "Golden Gate Bridge", "This is the Golden Gate Bridge")
  addLandmark(map, 37.802122, -122.418805, "Lombard Street", "This is Lombard St.")
  addLandmark(map, 37.811039, -122.415175, "Fishermans Wharf", "This is Fishermans Wharf")


}

