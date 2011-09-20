function loadScript(path, root) {
    var doc = window.document || win.getDocument();
    var head = doc.getElementsByTagName("head")[0] ||
    document.documentElement;
    var script = doc.createElement('script');
    script.type = 'text/javascript';
    script.src = (root || jasmine.plugin.rootDir) + path;
    head.appendChild(script);
}

function loadSource(fileName) {
    loadScript(fileName, jasmine.plugin.jsSrcDir);
}

function loadSpec(fileName) {
    loadScript(fileName, jasmine.plugin.jsTestDir);
}

