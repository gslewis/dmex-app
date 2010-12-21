/*
 * Value setting and focus traversal behaviour.
 *
 * For reverse-tab traversal, requires that the container of the last
 * focusable element have the class "lastFocusRow".  This assumes that the
 * first (left-most) <input> in the container is the last to receive focus
 * (which may not be right).
 *
 * TODO: <Enter> on the numpad should submit the form.
 */
$(document).ready(function() {
    // Set the next focus target when the user clicks in an <input> field.
    $(".problemFocus").focus(function() {
        var current = $(".problemFocus.hasFocus");
        if (this != current) {
            current.removeClass("hasFocus");
            $(this).addClass("hasFocus");
        }
    });

    // Do something similar with keypress for tabs so that focus moves
    // in right-to-left direction in input fields.
    $(".problemFocus").each(function(index) {
        var node = this;

        $(node).keypress(function(event) {
            // Handle tabbing in the "problemFocus" inputs.
            if (event.keyCode == 9) {
                handleTab(event, $(node));

            // Handle number entry in the "problemFocus" inputs
            } else if (event.which >= 48 && event.which <= 57) {
                setnodeval(String.fromCharCode(event.which), $(node));
                event.preventDefault();
            }
        });
    });

    // Make the numpad <td>s behave a bit button-like.
    $(".numpad td").mousedown(function() {
        var node = $(this);

        node.addClass("pressed");

        var rc = function() {
            node.removeClass("pressed");
        };

        node.mouseup(rc);
        node.mouseout(rc);
    });

    // Focus on the first focusable <input>.
    focusFirst();
}
);

function handleTab(event, node) {
    node.removeClass(".hasFocus");

    var next;
    if (event.shiftKey) {
        next = findPrev(node.attr("id"));
    } else {
        next = findNext(node.attr("id"));
    }

    if (next) {
        next.addClass(".hasFocus");
        next.focus();
    } else {
        // Was on the last <input>.  Need to exit the problemFocus
        // fields and go to the submit button.
        $("#problemForm input[type='submit']").focus();
    }

    // Suppress the normal tabbing behaviour.
    event.preventDefault();
}

function setval(digit) {
    // Find the node that currently has focus.  If none has focus, find the
    // first focusable input.
    var node = $(".problemFocus.hasFocus");
    if (node.length == 0) {
        node = focusFirst();
    }

    setnodeval(digit, node);
}

function setnodeval(digit, pnode) {
    var node = pnode;
    if (node == null || node.length == 0) {
        return;
    }

    var max = node.attr("maxlength");
    var value = node.attr("value");

    if (value.length < max) {
        node.attr("value", String.concat(value, digit));
    } else {
        node.attr("value", digit);
    }

    if (node.attr("value").length < max) {
        node.focus();
        return;
    }

    node.removeClass("hasFocus");
    node = findNext(node.attr("id"));

    if (node) {
        node.addClass("hasFocus");
        node.focus();
    } else {
        focusFirst();
    }
}

/*
 * Find the first focusable <input> that is blank.
 */
function focusFirst() {
    for (var i = 1; i < 99; ++i) {
        var node = $(focusId(i));

        if (node.length == 0) {
            break;
        } else if (node.attr("value") == "") {
            node.addClass("hasFocus");
            node.focus();

            return node;
        }
    }

    return $("#focus_1");
}

/*
 * Given the current focus id (cid), get the next focusable field.
 *
 * For example:
 *
 *     64
 *     61
 *   ----
 *     64           2<-1
 *   3840     5<-4<-3
 *   ----
 *   3904     9<-8<-7<-6
 *
 *   Note that the tens/hundreds/etc zero column of working rows are
 *   pre-filled with zeros so are not focusable.
 */
function findNext(cid) {
    var currentFocus = getFocusIndex(cid);
    if (currentFocus == 0) {
        return 0;
    }

    var node = $(focusId(currentFocus + 1));
    if (node.length) {
        return node;
    }

    return 0;
}

function findPrev(cid) {
    var currentFocus = getFocusIndex(cid);
    if (currentFocus == 0) {
        return 0;
    }

    var node;

    if (currentFocus > 1) {
        node = $(focusId(currentFocus - 1));
        if (node.length) {
            return node;
        }
    }

    // We were on the first input.  Try to find the last input in the last
    // focus container (answer row).
    // Note: Firebug reports ":first" as a CSS error (when it's not CSS and
    // should be ignored).
    node = $(".lastFocusRow input:first");
    return node;
}

function focusId(count) {
    return "#focus_" + count;
}

function getFocusIndex(cid) {
    var index = cid.indexOf("_") + 1;
    if (index == 0 || index == cid.length) {
        return 0;
    }

    return new Number(cid.slice(index));
}

function focusBack() {
    var node = $(".problemFocus.hasFocus");

    if (node.length) {
        node.removeClass("hasFocus");

        node = findPrev(node.attr("id"));
        if (node) {
            node.addClass(".hasFocus");
            node.focus();
        }
    }
}

function submitProblem() {
    $("#problemForm").submit();
}
