function createMDE(){
    return new EasyMDE({
      element: document.getElementById('message-input'),
      showIcons: ['table']
    });
}
