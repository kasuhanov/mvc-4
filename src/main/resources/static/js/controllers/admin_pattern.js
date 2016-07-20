app.controller('AdminPatternController', function($scope, $state, $http, $rootScope, $window, $uibModal) {
    $rootScope.title = "Паттерны";
    if($rootScope.authority != "ROLE_ADMIN"){
        $state.transitionTo('home');
    }
    init();
    function init() {
        $http.get('admin/pattern/all')
            .success(function(data) {
                $scope.patterns = data;
            });
    }
    $http.get('category/all')
        .success(function(data) {
            $scope.categories = data;
        });
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
    $scope.update = function () {
        $scope.updFailed = false;
        $http.get('admin/update')
            .success(function () {
                $window.location.reload();
            })
            .error(function () {
                $scope.updFailed = true;
            });
    };
    $scope.openPatternDialog = function () {
        var modalInstance = $uibModal.open({
            templateUrl: '../pages/new_pattern_dialog.html',
            controller: 'NewPatternDialogController',
            size: 'lg'
        });
        modalInstance.result.then(function () {
            $window.location.reload();
        });
    };
    $scope.openCategoryDialog = function () {
        var modalInstance = $uibModal.open({
            templateUrl: '../pages/new_category_dialog.html',
            controller: 'NewCategoryDialogController',
            size: 'lg'
        });
        modalInstance.result.then(function () {
            $window.location.reload();
        });
    };
    $scope.openDeleteCategoryDialog = function () {
        var modalInstance = $uibModal.open({
            templateUrl: '../pages/delete_category_dialog.html',
            controller: 'DeleteCategoryDialogController',
            size: 'lg'
        });
        modalInstance.result.then(function () {
            $window.location.reload();
        });
    };
});
