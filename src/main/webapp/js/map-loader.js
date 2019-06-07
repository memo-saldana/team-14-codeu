function createMap(){
  const map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 37.764419, lng: -122.445911},
    zoom: 12
  });

  const bridgeMarker = new google.maps.Marker({
    position: {lat: 37.819021, lng: -122.477946},
    map: map,
    title: 'Golden Gate Bridge'
  });

  const bridgeInfoWindow = new google.maps.InfoWindow({
    content: "This is the Golden Gate Bridge"
  });
  bridgeInfoWindow.open(map, bridgeMarker);

}