$(document).ready(
function() {
    $(".CheckboxMultiNumberSelection input[type=checkbox]").click(
        function() {
            var index = this.name.indexOf('.all');

            if (index != -1) {
                // If the "All" sb is selected/deselected, modify all the
                // value cbs.
                var gname = this.name.substring(0, index);

                $(".CheckboxMultiNumberSelection input[name='" + gname + "']")
                    .attr("checked", this.checked);

            } else if (!this.checked) {
                // If any value cb is deselected, deselect the "All" cb.
                $(".CheckboxMultiNumberSelection input[name='" + this.name
                    + ".all']").attr("checked", false);

            } else {
                // If all value cbs are selected, select the "All" cb.
                var allSelected = true;
                $(".CheckboxMultiNumberSelection input[name='"+this.name+"']")
                .each(
                    function() {
                        if (!this.checked) {
                            allSelected = false;
                        }
                    });

                if (allSelected) {
                    $(".CheckboxMultiNumberSelection input[name='" + this.name
                        + ".all']").attr("checked", true);
                }
            }
        });
});
