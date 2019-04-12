// Get message displayed after a successful renewal
function getRenewalMessage(){
    let content = document.querySelector('.ui-content');
    if (content == null) return;

    let nodes = content.childNodes;
    let contents = new Array(0);
    for (let i = 0; i < nodes.length; ++i)
        if (nodes[i].nodeType == Node.TEXT_NODE && nodes[i].textContent.trim())
            contents.push(nodes[i].textContent.trim());

    let details = {
        details : contents,
        featured : content.querySelector('.textoNegrito').textContent
    };
    js_api.setConfirmationMessage(JSON.stringify(details));
}