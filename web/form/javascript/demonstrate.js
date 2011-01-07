/*
 * Demonstrates the solving of a problem.
 *
 * Makes an ajax call to http://<host>/problem/<eid>?action=demo
 * Receives a JSON-format solution script.
 */
function demonstrate(eid) {
    $.ajax({
        url: eid + '?action=demo',
        type: 'GET',
        cache: false,
        dataType: 'json',
        success: function(data) {
            if (data.length > 0) {
                var overlay = document.createElement("div");
                overlay.setAttribute("id", "overlay");
                overlay.setAttribute("class", "overlay");
                document.body.appendChild(overlay);

                showDemo(overlay, data, 0);
            }
        }
    });
}

function showDemo(overlay, demolist, index) {
    if (index >= demolist.length) {
        closeDemo(overlay, null);
        return;
    }

    var entry = demolist[index];

    // Array of target/class maps.  The "class" is applied to each target at
    // the start of the step and removed at the end of the step.
    var targets = null;
    if ('target' in entry) {
        targets = [ { "target": entry.target, "class": entry['class'] } ];
    } else if ('targets' in entry) {
        targets = entry.targets;
    }

    // Array of target/value maps.  Values are applied to the "value"
    // attribute of the <input> identified by the target.
    var actions = null;
    if ('action' in entry) {
        actions = [ entry.action ];
    } else if ('actions' in entry) {
        actions = entry.actions;
    } else if ('value' in entry && 'target' in entry) {
        actions = [ { "target": entry.target, "value": entry.value } ];
    }

    var content = document.createElement("div");
    content.setAttribute("id", "demo");
    content.setAttribute("class", "demo");

    doLayout(content);

    content.appendChild(button('X', 'closeButton',
                function() { closeDemo(overlay, targets); }));

    content.appendChild(textContainer(entry.text));

    if (targets) {
        doTargets(targets, 'addClass');
    }

    if (actions) {
        doActions(actions);
    }
    
    var next = index + 1;
    if (next < demolist.length) {
        content.appendChild(button('Next', 'nextButton',
            function() {
                overlay.removeChild(content);
                if (targets) {
                    doTargets(targets, 'removeClass');
                }

                showDemo(overlay, demolist, next);
            }));
    } else {
        content.appendChild(button('Done', 'nextButton',
            function() {
                closeDemo(overlay, targets);
            }));
    }

    overlay.appendChild(content);
}

function doTargets(targets, action) {
    var i;
    for (i = 0; i < targets.length; ++i) {
        var target = $(targets[i].target);
        var targetCls = targets[i]['class'];

        if (target) {
            if (action == 'addClass') {
                target.addClass(targetCls);
            } else {
                target.removeClass(targetCls);
            }
        }
    }
}

function doActions(actions) {
    var i;
    for (i = 0; i < actions.length; ++i) {
        var target = $(actions[i].target);

        if (target) {
            if ('value' in actions[i]) {
                target.attr('value', actions[i].value);
            } else {
                target.attr('value', "");
            }
        }
    }
}

function doLayout(content) {
    var pc = $("div#problemContainer");

    var vw = $(window).width();
    var pco = pc.offset();
    var pcw = pc.width();

    // Calculate the space in the viewport to the right of the problem
    // container.
    var rs = vw - pco.left - pcw;
    if (rs >= 240) {
        // If there is space for the box to the right
        var x = pco.left + pcw + 10;
        $(content).offset({ top: pco.top, left: x });

        // If we can't fit the 360px box, shrink it
        if (rs < 372) {
            $(content).width(rs - 26);
        }
    } else {
        // No space to the right: lay over the numpad.
        var np = $("table.numpad");
        var npo = np.offset();

        $(content).width(np.width() + 20);
        $(content).offset({ top: npo.top, left: npo.left - 10 });
    } 
}

function closeDemo(overlay, targets) {
    if (targets) {
        var i;
        for (i = 0; i < targets.length; ++i) {
            $(targets[i].target).removeClass(targets[i]['class']);
        }
    }

    document.body.removeChild(overlay);
}

// Text content may have HTML markup.
// Text may be a string, or an array of strings.
function textContainer(text) {
    var container = document.createElement("div");
    container.setAttribute("class", "demo-text");

    if (typeof text === 'string') {
        container.innerHTML = text;
    } else {
        // Assume 'text' is an array.
        var content = "";
        var i;
        for (i = 0; i < text.length; ++i) {
            content += '<p>' + text[i] + '</p>';
        }

        container.innerHTML = content;
    }

    return container;
}

function button(text, buttonClass, action) {
    var button = document.createElement("input");
    button.setAttribute("type", "button");
    button.setAttribute("value", text);

    if (buttonClass) {
        button.setAttribute("class", buttonClass);
    }

    $(button).click(action);

    return button;
}
