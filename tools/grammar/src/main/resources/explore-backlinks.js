window.onload=function(){
    var elements = document.getElementsByClassName("backlink");
    for (let i = 0; i < elements.length; i++) {
        var item = elements[i];
        item.onclick = openBacklink;
        item.className += ' unexplored';
        var refName = item.getAttribute('backlink');
        var def = backlinks[refName];
        if (def) {
            var a = item.firstChild;
            a.setAttribute('title', def.bnf);
        }
    }
};

function openBacklink(e) {
    e.stopImmediatePropagation();
    var item = e.srcElement || e.target;

    var list = item.lastChild;
    if (list.tagName === 'UL') {
        if (list.style.display === 'none') {
            item.className = '';
            list.style.display = '';
        } else {
            item.className = 'unexplored';
            list.style.display = 'none';
        }
    } else {
        item.className = '';
        console.log("new list!", list.tagName, list);
        var refName = item.getAttribute('backlink');
        var def = backlinks[refName];
        if (def) {
            var refs = def.references;
            var items = "";
            for (let i = 0; i < refs.length; i++) {
                var ref = refs[i];
                var refDef = backlinks[ref];
                items += '<li class="unexplored" onclick="openBacklink" backlink="' + ref + '"><a href="' + refDef.link + '" title="' + refDef.bnf + '">' + ref + '</a></li>';
            }
            item.innerHTML += '<ul class="backlinks">' + items + '</ul>';
        }
    }
}
