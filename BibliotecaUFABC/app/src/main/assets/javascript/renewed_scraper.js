// Get message displayed after a successful renewal
function getRenewalMessage(){
    let content = document.querySelector('.ui-content');
    if (content == null) return;

    let nodes = content.childNodes;
    let contents = new Array(0);
    for (let i = 0; i < nodes.length; ++i)
        if (nodes[i].nodeType == Node.TEXT_NODE && nodes[i].textContent.trim())
            contents.push(nodes[i].textContent.trim());

    let featuredNode = content.querySelector('.textoNegrito');
    if (featuredNode == null) featuredNode = content.querySelector('.textoVermelho');

    return {
        details : contents,
        featured : featuredNode == null? "" : featuredNode.textContent
    };
}