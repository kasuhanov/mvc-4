app.controller('SubscribeController', function($scope, $http, $rootScope) {
    $rootScope.title = "Подписка";
    init();
    $rootScope.$watch('authenticated', function(){
        init();
    });
    function init(){
        if($rootScope.authenticated){
            $http.get('subscribe/notify-favs')
                .success(function(data){
                    $scope.notify = data;
                });
            $http.get('subscribe/')
                .success(function(data) {
                    $scope.categories = data;
                    $scope.categories.forEach(function(item) {
                        $scope.selected[item.id] = !!item.subscribed;
                    });
                });
        }
    }
    $scope.selected = {};
    $scope.subscribe = function(id){
        $http.get('subscribe/sub/?id='+id+'&sel='+!$scope.selected[id])
            .success(function() {
                $scope.selected[id]=!$scope.selected[id];
            });
    };
    $scope.changeNotify = function () {
        $http.post('subscribe/notify-favs', !$scope.notify)
            .success(function() {
                $scope.notify = !$scope.notify;
            });
    }
});