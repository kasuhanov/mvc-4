app.controller('OkpdDialogController', function($scope, $state, $http, $uibModalInstance, okpd) {
    $scope.okpd = okpd;
    $scope.newPattern ={};
    $scope.newPattern.pattern = okpd;
    $http.get('category/all')
        .success(function(data) {
            $scope.categories = data;
        });
    init();
    function init() {
        $http.get('admin/pattern/okpd/'+encodeURIComponent(okpd.replace(/\./g, '&#46;')))
            .success(function(data) {
                $scope.patterns = data;
            });
    }
    $scope.remove = function (pattern,cat) {
        $http.get('admin/pattern/'+pattern.id+'/remove-category/'+cat)
            .success(function() {
                init();
            });
    };
    $scope.add = function (pattern,cat) {
        var inArr = false;
        pattern.categories.forEach(function(item) {
            if(item.id == cat)
                inArr = true;
        });
        if ((cat !== undefined)&&(!inArr)){
            $http.get('admin/pattern/'+pattern.id+'/add-category/'+cat)
                .success(function() {
                    init();
                });
        }
    };
    $scope.deletePattern = function (id) {
        $http.delete('admin/pattern/'+id)
            .success(function() {
                init();
            });

    };
    $scope.submit = function(){
        $scope.failed = false;
        $scope.success = false;
        $http.post('admin/pattern/'+encodeURIComponent($scope.newPattern.pattern.replace(/\./g, '&#46;')), $scope.newPattern.categories)
            .success(function () {
                $scope.failed = false;
                $scope.success = true;
                init();
            })
            .error(function () {
                $scope.failed = true;
                $scope.success = false;
            });
    };
    $scope.closeDialog = function(){
        $uibModalInstance.close();
    };
});
