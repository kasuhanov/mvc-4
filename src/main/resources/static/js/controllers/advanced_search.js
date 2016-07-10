app.controller('AdvancedSearchController', function($scope, $http, $state, $stateParams, $rootScope, $uibModal, paginate) {
    $rootScope.title = "Поиск";
    $scope.search = $stateParams;
    $scope.newSearch = angular.copy($scope.search);
    if($stateParams.customerDialog==='true')
        openCustomerDialog();
    if($stateParams.hideFilter !== undefined){
        $scope.hideFilter = $stateParams.hideFilter === 'true';
    }
    else
        $scope.hideFilter = false;
    if($stateParams.startDate !== undefined)
        $scope.search.startDate = new Date(Number($stateParams.startDate));
    if($stateParams.endDate !== undefined)
        $scope.search.endDate = new Date(Number($stateParams.endDate));
    if($stateParams.customer !== undefined){
        $http.get('customer/'+$stateParams.customer)
            .success(function(data) {
                $scope.customer = data;
            });

    }
    search($scope.search);
    function search(params){
        $scope.currentPage =  $stateParams.page || 1;
        $scope.url='search/advanced_paged?';
        if(params.id) {
            $scope.url+="&id="+params.id;
        }
        if(params.purchaseName) {
            $scope.url+="&purchaseName="+params.purchaseName;
        }
        if(params.customer) {
            $scope.url+="&customer="+params.customer;
        }
        if(params.startDate) {
            $scope.url+="&startDate="+params.startDate.getTime();
        }
        if(params.endDate) {
            $scope.url+="&endDate="+params.endDate.getTime();
        }
        if(params.minPrice) {
            $scope.url+="&minPrice="+params.minPrice;
        }
        if(params.maxPrice) {
            $scope.url+="&maxPrice="+params.maxPrice;
        }
        if(params.fz) {
            $scope.url+="&fz="+params.fz;
        }
        if(params.quick) {
            $scope.url+="&quick="+params.quick;
        }
        if(params.category) {
            $scope.url+="&category="+params.category;
            $http.get("category/"+params.category)
                .success(function(data) {
                    $scope.category = data;
                    $rootScope.title = $scope.category.name;
                });
        }
        if(params.type) {
            $scope.url+="&type="+params.type;
        }
        if(params.completed) {
            $scope.url+="&completed="+params.completed;
        }
        $scope.orderby = params.orderby===undefined?'submissionCloseDate':params.orderby;
        $scope.order = params.order===undefined?'desc':params.order;
        $scope.url+='&order='+$scope.order+'&orderby='+$scope.orderby+'&&pageSize=';
        $scope.result=true;
        paginate($scope,$http,$state,$stateParams);
    }
    $scope.calendar1 = {
        open: false
    };
    $scope.calendar2 = {
        open: false
    };
    $scope.open1 = function() {
        $scope.calendar1.open = true;
    };
    $scope.open2 = function() {
        $scope.calendar2.open = true;
    };
    $http.get('category/all')
        .success(function(data) {
            $scope.categories = data;
        });
    $scope.submit = function(){
        $scope.search = angular.copy($scope.newSearch);
        $scope.search.quick = null;
        $scope.newSearch.quick = null;
        $rootScope.search.text = null;
        var stateParams = {};
        $scope.currentPage = 1;
        stateParams.page  = 1;
        stateParams.hideFilter=$scope.hideFilter;
        if($scope.search.id)
            stateParams.id=$scope.search.id;
        if($scope.search.purchaseName)
            stateParams.purchaseName=$scope.search.purchaseName;
        if($scope.search.customer)
            stateParams.customer=$scope.search.customer;
        if($scope.search.startDate) {
            stateParams.startDate=$scope.search.startDate.getTime();
        }
        if($scope.search.endDate) {
            stateParams.endDate=$scope.search.endDate.getTime();
        }
        if($scope.search.minPrice) {
            stateParams.minPrice=$scope.search.minPrice;
        }
        if($scope.search.maxPrice) {
            stateParams.maxPrice=$scope.search.maxPrice;
        }
        if($scope.search.category) {
            stateParams.category=$scope.search.category;
        }
        if($scope.search.type) {
            stateParams.type=$scope.search.type;
        }
        if($scope.search.completed) {
            stateParams.completed=$scope.search.completed;
        }
        if($scope.search.fz) {
            stateParams.fz=$scope.search.fz;
        }
        if($rootScope.search.text) {
            //stateParams.quick=$rootScope.search.text;
            stateParams.quick = null;
        }
        search($scope.search);
        $state.transitionTo('search',stateParams,{notify:false});
    };

    $scope.clear = function () {
        $scope.customer = null;
        $scope.category = null;
        $scope.newSearch = {};
    };

    $scope.toggleFilter = function () {
        $scope.hideFilter = !$scope.hideFilter;
    };

    $scope.changeOrder = function(orderby){
        var stateParams = $stateParams;
        stateParams.orderby = orderby;
        if($scope.orderby == orderby){
            if($scope.order == 'asc')
                stateParams.order = 'desc';
            else
                stateParams.order = 'asc';
        }else{
            stateParams.order = 'asc';
        }
        stateParams.page = 1;
        $scope.search.order = stateParams.order;
        $scope.search.orderby = stateParams.orderby;
        search($scope.search);
        $state.transitionTo($state.current.name,stateParams,{notify:false});
    };

    $scope.openCustomerDialog = function () {
        openCustomerDialog();
    };

    function openCustomerDialog() {
        $stateParams.customerDialog = true;
        $state.transitionTo($state.current.name,$stateParams,{notify:false});
        $scope.modalInstance = $uibModal.open({
            templateUrl: '../pages/customer_dialog.html',
            controller: 'CustomerDialogController',
            size: 'lg'
        });
        $scope.modalInstance.result.then(function (customer) {
            $stateParams.customer = customer.id;
            $scope.newSearch.customer = customer.id;
            $scope.customer = customer;
        });
        $scope.modalInstance.closed.then(function () {
            $stateParams.customerDialog = null;
            $stateParams.corder = null;
            $stateParams.corderby = null;
            $stateParams.cpage = null;
            $stateParams.query = null;
            $state.transitionTo($state.current.name,$stateParams,{notify:false});
        });
    }
});