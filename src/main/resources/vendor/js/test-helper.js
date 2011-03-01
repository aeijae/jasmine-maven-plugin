function loadScript(path) {
    var doc = window.document || win.getDocument();
    var head = doc.getElementsByTagName("head")[0] ||
    document.documentElement;
    var script = doc.createElement('script');
    script.type = 'text/javascript';
    script.src = path;
    head.appendChild(script);
}

function loadSource(fileName) {
    loadScript(jasmine.plugin.jsSrcDir + fileName);
}

function loadSpec(fileName) {
    loadScript(jasmine.plugin.jsTestDir + fileName);
}

